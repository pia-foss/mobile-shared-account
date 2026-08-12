package com.privateinternetaccess.account.internals.persistency.datastore

import android.content.Context
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore

/**
 * Android-backed encrypted key/value store: a Preferences DataStore whose values are encrypted
 * with a Tink AEAD primitive wrapping an Android Keystore key.
 *
 * On first initialization, any values still sitting in the legacy `EncryptedSharedPreferences`
 * store (see KM-17766) are migrated in and the legacy store is deleted.
 */
internal actual object EncryptedKeyValueStore {

    private const val DATASTORE_FILE_NAME = "account_datastore.preferences_pb"
    private const val LEGACY_PREFS_NAME = "account_shared_preferences"
    private const val KEYSET_PREFS_NAME = "account_tink_keyset_prefs"
    private const val KEYSET_ALIAS = "account_tink_keyset"
    private const val MASTER_KEY_ALIAS = "com.privateinternetaccess.account_tink_master_key"
    private const val MASTER_KEY_URI = "android-keystore://$MASTER_KEY_ALIAS"

    private val LEGACY_MIGRATION_COMPLETED_KEY = booleanPreferencesKey("legacy_migration_completed")

    private val initLock = Mutex()
    private val cache = mutableMapOf<String, String>()

    @Volatile
    private var dataStore: DataStore<Preferences>? = null

    @Volatile
    private var aead: Aead? = null

    actual suspend fun putString(key: String, value: String) {
        val store = ensureInitialized() ?: return
        val encrypted = encrypt(value)
        store.edit { it[stringPreferencesKey(key)] = encrypted }
        synchronized(cache) { cache[key] = value }
    }

    actual suspend fun getString(key: String): String? {
        val store = ensureInitialized() ?: return synchronized(cache) { cache[key] }
        val value = readDecrypted(store, key)
        synchronized(cache) {
            if (value != null) cache[key] = value else cache.remove(key)
        }
        return value
    }

    actual suspend fun remove(key: String) {
        val store = ensureInitialized()
        store?.edit { it.remove(stringPreferencesKey(key)) }
        synchronized(cache) { cache.remove(key) }
    }

    actual fun cachedString(key: String): String? = synchronized(cache) { cache[key] }

    // region private

    private suspend fun ensureInitialized(): DataStore<Preferences>? {
        val context = AccountContextProvider.applicationContext ?: return null
        dataStore?.let { return it }
        initLock.withLock {
            dataStore?.let { return it }
            val store = PreferenceDataStoreFactory.create(
                produceFile = { context.applicationContext.preferencesDataStoreFile(DATASTORE_FILE_NAME) }
            )
            migrateLegacyStoreIfNeeded(context, store)
            warmCache(store)
            dataStore = store
            return store
        }
    }

    private suspend fun warmCache(store: DataStore<Preferences>) {
        val prefs = currentPreferences(store)
        synchronized(cache) {
            cache.clear()
            listOf(Account.API_TOKEN_KEY, Account.VPN_TOKEN_KEY).forEach { key ->
                prefs[stringPreferencesKey(key)]?.let { encrypted ->
                    runCatching { decrypt(encrypted) }.getOrNull()?.let { cache[key] = it }
                }
            }
        }
    }

    private suspend fun readDecrypted(store: DataStore<Preferences>, key: String): String? {
        val encrypted = currentPreferences(store)[stringPreferencesKey(key)] ?: return null
        return runCatching { decrypt(encrypted) }.getOrNull()
    }

    private suspend fun currentPreferences(store: DataStore<Preferences>): Preferences =
        store.data.catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.first()

    private suspend fun migrateLegacyStoreIfNeeded(context: Context, store: DataStore<Preferences>) {
        if (currentPreferences(store)[LEGACY_MIGRATION_COMPLETED_KEY] == true) return

        readLegacyPreferences(context)?.forEach { (key, value) ->
            store.edit { it[stringPreferencesKey(key)] = encrypt(value) }
        }
        deleteLegacyStore(context)

        store.edit { it[LEGACY_MIGRATION_COMPLETED_KEY] = true }
    }

    private fun readLegacyPreferences(context: Context): Map<String, String>? {
        val legacyPrefs = try {
            buildLegacyEncryptedSharedPreferences(context)
        } catch (e: GeneralSecurityException) {
            // Keyset unrecoverable. Nothing to migrate; the crash-fix path (KM-17766) already
            // treats this data as unrecoverable, so we just move on with a clean slate.
            null
        } ?: return null

        return setOf(Account.API_TOKEN_KEY, Account.VPN_TOKEN_KEY)
            .mapNotNull { key -> legacyPrefs.getString(key, null)?.let { key to it } }
            .toMap()
    }

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

    private fun deleteLegacyStore(context: Context) {
        context.applicationContext.deleteSharedPreferences(LEGACY_PREFS_NAME)
        runCatching {
            KeyStore.getInstance("AndroidKeyStore").apply {
                load(null)
                deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            }
        }
    }

    private fun encrypt(value: String): String {
        val ciphertext = aead().encrypt(value.toByteArray(Charsets.UTF_8), null)
        return Base64.encodeToString(ciphertext, Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String {
        val plaintext = aead().decrypt(Base64.decode(value, Base64.NO_WRAP), null)
        return String(plaintext, Charsets.UTF_8)
    }

    private fun aead(): Aead {
        aead?.let { return it }
        val context = requireNotNull(AccountContextProvider.applicationContext) {
            "Account context not available"
        }
        AeadConfig.register()
        val primitive = try {
            buildAead(context)
        } catch (e: GeneralSecurityException) {
            resetKeyset(context)
            buildAead(context)
        }
        aead = primitive
        return primitive
    }

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