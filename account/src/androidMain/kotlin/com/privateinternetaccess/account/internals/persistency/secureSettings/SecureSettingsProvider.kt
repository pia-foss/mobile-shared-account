package com.privateinternetaccess.account.internals.persistency.secureSettings

import com.privateinternetaccess.account.internals.AccountContextProvider
import com.privateinternetaccess.account.internals.EncryptedSettingsFactory
import com.russhwolf.settings.Settings

internal actual object SecureSettingsProvider {

    private const val SHARED_PREFS_NAME = "account_shared_preferences"

    actual val settings: Settings?
        get() = AccountContextProvider.applicationContext?.let { context ->
            EncryptedSettingsFactory(context).create(SHARED_PREFS_NAME)
        }
}