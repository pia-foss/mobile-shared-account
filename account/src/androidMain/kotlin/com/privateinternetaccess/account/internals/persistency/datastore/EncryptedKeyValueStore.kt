package com.privateinternetaccess.account.internals.persistency.datastore

import android.content.Context
import android.os.SystemClock
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.privateinternetaccess.account.internals.Account
import com.privateinternetaccess.account.internals.AccountContextProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.ProviderException

/**
 * Android-backed encrypted key/value store: a Preferences DataStore whose values are encrypted
 * with a Tink AEAD primitive wrapping an Android Keystore key.
 *
 * On first initialization, any values still sitting in the legacy `EncryptedSharedPreferences`
 * store (see KM-17766) are migrated in and the legacy store is deleted.
 *
 * Right after a device boot the Android Keystore can transiently fail (notably on Fire OS), so
 * building the AEAD primitive is retried with backoff. A value that can't be decrypted is never
 * reported as absent: callers treat a missing token as "request a new one", which floods the API.
 * The last known value is served instead. For the same reason the keyset is only reset when that
 * can't orphan stored values because of a transient post-boot failure (see [keysetResetAllowed]).
 *
 * All store work runs on [Dispatchers.IO], never on the caller's dispatcher. [cachedString] may
 * block the calling (usually main) thread on initialization, and that must not wait on work that
 * needs the blocked thread to make progress.
 */
internal actual object EncryptedKeyValueStore {

    private const val DATASTORE_FILE_NAME = "account_datastore.preferences_pb"
    private const val LEGACY_PREFS_NAME = "account_shared_preferences"
    private const val KEYSET_PREFS_NAME = "account_tink_keyset_prefs"
    private const val KEYSET_ALIAS = "account_tink_keyset"
    private const val MASTER_KEY_ALIAS = "com.privateinternetaccess.account_tink_master_key"
    private const val MASTER_KEY_URI = "android-keystore://$MASTER_KEY_ALIAS"

    private const val AEAD_BUILD_ATTEMPTS = 5
    private const val AEAD_RETRY_BASE_DELAY_MS = 200L

    // Past this uptime, a Keystore failure is no longer the transient post-boot kind.
    private const val KEYSET_RESET_MIN_UPTIME_MS = 5 * 60 * 1000L

    private val LEGACY_MIGRATION_COMPLETED_KEY = booleanPreferencesKey("legacy_migration_completed")
    private val TOKEN_KEYS = listOf(Account.API_TOKEN_KEY, Account.VPN_TOKEN_KEY)

    private val initScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val initLock = Any()
    private val aeadLock = Mutex()
    private val cache = mutableMapOf<String, String>()

    @Volatile
    private var dataStore: DataStore<Preferences>? = null

    @Volatile
    private var initialization: Deferred<DataStore<Preferences>>? = null

    @Volatile
    private var aead: Aead? = null

    actual suspend fun putString(key: String, value: String): Unit = withContext(Dispatchers.IO) {
        val store = ensureInitialized()
        // Cached first, so this process keeps using the new value even if persisting it fails.
        synchronized(cache) { cache[key] = value }
        if (store == null) return@withContext
        try {
            val encrypted = encrypt(value, allowKeysetReset = keysetResetAllowed(store))
            store.edit { it[stringPreferencesKey(key)] = encrypted }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Keystore or disk unavailable. The value stays in memory and the next write retries.
        }
    }

    actual suspend fun getString(key: String): String? = withContext(Dispatchers.IO) {
        val store = ensureInitialized() ?: return@withContext cachedValue(key)
        val encrypted = currentPreferences(store)[stringPreferencesKey(key)]
        if (encrypted == null) {
            synchronized(cache) { cache.remove(key) }
            return@withContext null
        }
        // Unreadable is not absent: keep serving the last known value.
        val value = decryptOrNull(encrypted) ?: return@withContext cachedValue(key)
        synchronized(cache) { cache[key] = value }
        value
    }

    actual suspend fun remove(key: String): Unit = withContext(Dispatchers.IO) {
        val store = ensureInitialized()
        synchronized(cache) { cache.remove(key) }
        try {
            store?.edit { it.remove(stringPreferencesKey(key)) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            // Disk unavailable. The value is already gone from memory for this process.
        }
    }

    actual fun cachedString(key: String): String? {
        val pending = startInitialization()
        if (pending != null && !pending.isCompleted) {
            // Cold start: answering from an empty cache would report "no token" for a stored one.
            runBlocking { runCatching { pending.await() } }
        }
        return cachedValue(key)
    }

    // region private

    private fun cachedValue(key: String): String? = synchronized(cache) { cache[key] }

    private suspend fun ensureInitialized(): DataStore<Preferences>? {
        val pending = startInitialization() ?: return null
        return try {
            pending.await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Let the next call try again.
            synchronized(initLock) {
                if (initialization === pending) initialization = null
            }
            null
        }
    }

    private fun startInitialization(): Deferred<DataStore<Preferences>>? {
        initialization?.let { return it }
        val context = AccountContextProvider.applicationContext ?: return null
        synchronized(initLock) {
            initialization?.let { return it }
            // Only one DataStore may ever be active for the file, even across initialization retries.
            val store = dataStore ?: PreferenceDataStoreFactory.create(
                produceFile = { context.applicationContext.preferencesDataStoreFile(DATASTORE_FILE_NAME) }
            ).also { dataStore = it }
            return initScope.async {
                migrateLegacyStoreIfNeeded(context, store)
                warmCache(store)
                store
            }.also { initialization = it }
        }
    }

    private suspend fun warmCache(store: DataStore<Preferences>) {
        val prefs = currentPreferences(store)
        val decrypted = TOKEN_KEYS.mapNotNull { key ->
            prefs[stringPreferencesKey(key)]?.let { encrypted ->
                decryptOrNull(encrypted)?.let { key to it }
            }
        }
        synchronized(cache) {
            cache.clear()
            cache.putAll(decrypted)
        }
    }

    private suspend fun decryptOrNull(encrypted: String): String? =
        try {
            decrypt(encrypted)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }

    private suspend fun currentPreferences(store: DataStore<Preferences>): Preferences =
        store.data.catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.first()

    /**
     * A keyset reset discards every value encrypted with it. That is only acceptable when nothing
     * is stored yet, or when the device has been up long enough that the Keystore failure isn't
     * the transient post-boot one and the stored values are unrecoverable anyway.
     */
    private suspend fun keysetResetAllowed(store: DataStore<Preferences>): Boolean {
        if (SystemClock.elapsedRealtime() >= KEYSET_RESET_MIN_UPTIME_MS) return true
        val prefs = currentPreferences(store)
        return TOKEN_KEYS.none { prefs[stringPreferencesKey(it)] != null }
    }

    private suspend fun migrateLegacyStoreIfNeeded(context: Context, store: DataStore<Preferences>) {
        if (currentPreferences(store)[LEGACY_MIGRATION_COMPLETED_KEY] == true) return

        try {
            readLegacyPreferences(context)?.forEach { (key, value) ->
                val encrypted = encrypt(value, allowKeysetReset = keysetResetAllowed(store))
                store.edit { it[stringPreferencesKey(key)] = encrypted }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Keystore not ready. Keep the legacy store around and try again on the next start.
            return
        }
        context.applicationContext.deleteSharedPreferences(LEGACY_PREFS_NAME)

        store.edit { it[LEGACY_MIGRATION_COMPLETED_KEY] = true }
    }

    private fun readLegacyPreferences(context: Context): Map<String, String>? {
        // Building EncryptedSharedPreferences creates its master key, so don't touch a store that
        // was never written.
        val legacyFile = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
        if (legacyFile.all.isEmpty()) return null

        val legacyPrefs = try {
            buildLegacyEncryptedSharedPreferences(context)
        } catch (e: GeneralSecurityException) {
            // Keyset unrecoverable. Nothing to migrate; the crash-fix path (KM-17766) already
            // treats this data as unrecoverable, so we just move on with a clean slate.
            null
        } ?: return null

        return TOKEN_KEYS
            .mapNotNull { key -> legacyPrefs.getString(key, null)?.let { key to it } }
            .toMap()
    }

    // The legacy master key is deliberately not deleted: it is the app-wide default
    // `MasterKey` alias, which the consuming app may use for its own encrypted preferences.
    private fun buildLegacyEncryptedSharedPreferences(context: Context) = EncryptedSharedPreferences.create(
        context,
        LEGACY_PREFS_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setUserAuthenticationRequired(false)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private suspend fun encrypt(value: String, allowKeysetReset: Boolean): String {
        val ciphertext = aead(allowKeysetReset).encrypt(value.toByteArray(Charsets.UTF_8), null)
        return Base64.encodeToString(ciphertext, Base64.NO_WRAP)
    }

    private suspend fun decrypt(value: String): String {
        val plaintext = aead(allowKeysetReset = false).decrypt(Base64.decode(value, Base64.NO_WRAP), null)
        return String(plaintext, Charsets.UTF_8)
    }

    /**
     * Returns the AEAD primitive, retrying while the Keystore may still be coming up. Failures are
     * not cached, so a later call tries again. The keyset is only reset (discarding every value
     * encrypted with it) when [allowKeysetReset] is set and all attempts have failed.
     */
    private suspend fun aead(allowKeysetReset: Boolean): Aead {
        aead?.let { return it }
        aeadLock.withLock {
            aead?.let { return it }
            val context = requireNotNull(AccountContextProvider.applicationContext) {
                "Account context not available"
            }
            AeadConfig.register()
            val primitive = try {
                buildAeadWithRetry(context)
            } catch (e: Exception) {
                if (!allowKeysetReset || !e.isKeysetFailure()) throw e
                resetKeyset(context)
                buildAead(context)
            }
            aead = primitive
            return primitive
        }
    }

    private suspend fun buildAeadWithRetry(context: Context): Aead {
        var attempt = 0
        while (true) {
            try {
                return buildAead(context)
            } catch (e: Exception) {
                if (!e.isKeysetFailure() || ++attempt >= AEAD_BUILD_ATTEMPTS) throw e
                delay(AEAD_RETRY_BASE_DELAY_MS * attempt)
            }
        }
    }

    // Tink surfaces Keystore/keyset problems as GeneralSecurityException, ProviderException or,
    // when a keyset can't be parsed, IOException (InvalidProtocolBufferException).
    private fun Exception.isKeysetFailure(): Boolean =
        this is GeneralSecurityException || this is ProviderException || this is IOException

    private fun buildAead(context: Context): Aead =
        AndroidKeysetManager.Builder()
            .withSharedPref(context.applicationContext, KEYSET_ALIAS, KEYSET_PREFS_NAME)
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
            .getPrimitive(Aead::class.java)

    private fun resetKeyset(context: Context) {
        context.applicationContext
            .getSharedPreferences(KEYSET_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        runCatching {
            KeyStore.getInstance("AndroidKeyStore").apply {
                load(null)
                deleteEntry(MASTER_KEY_ALIAS)
            }
        }
    }
    // endregion
}
