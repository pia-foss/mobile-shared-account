package com.privateinternetaccess.account.internals

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.SharedPreferencesSettings
import com.russhwolf.settings.Settings
import java.security.GeneralSecurityException
import java.security.KeyStore

internal class EncryptedSettingsFactory(private val context: Context) : Settings.Factory {

    override fun create(name: String?): Settings {
        val preferencesName = name ?: "${context.packageName}_preferences"
        return SharedPreferencesSettings(createEncryptedSharedPreferences(preferencesName))
    }

    // region private
    private fun createEncryptedSharedPreferences(preferencesName: String) =
        try {
            buildEncryptedSharedPreferences(preferencesName)
        } catch (e: GeneralSecurityException) {
            // The keyset backing this store can no longer be decrypted (e.g. the
            // Keystore-backed master key was invalidated/evicted, or the prefs file
            // was restored onto a device without its device-bound key). Either way
            // the original contents are unrecoverable, so drop the corrupted store
            // and its master key and start clean instead of crashing on every launch.
            resetCorruptedStore(preferencesName)
            buildEncryptedSharedPreferences(preferencesName)
        }

    private fun buildEncryptedSharedPreferences(preferencesName: String) =
        EncryptedSharedPreferences.create(
            context,
            preferencesName,
            buildMasterKey(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    private fun buildMasterKey() =
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setUserAuthenticationRequired(false)
            .build()

    private fun resetCorruptedStore(preferencesName: String) {
        context.deleteSharedPreferences(preferencesName)
        runCatching {
            KeyStore.getInstance("AndroidKeyStore").apply {
                load(null)
                deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            }
        }
    }
    // endregion
}