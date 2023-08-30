package com.privateinternetaccess.account.internals.persistency.secureSettings

import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings

internal actual object SecureSettingsProvider {

    private const val KEYCHAIN_NAME = "account_keychain"

    actual val settings: Settings?
        get() = KeychainSettings(KEYCHAIN_NAME)
}