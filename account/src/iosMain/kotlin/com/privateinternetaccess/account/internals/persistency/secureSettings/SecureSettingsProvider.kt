package com.privateinternetaccess.account.internals.persistency.secureSettings

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.CFBridgingRetain
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAlways
import platform.Security.kSecAttrService

internal actual object SecureSettingsProvider {

    private const val KEYCHAIN_NAME = "account_keychain"

    @OptIn(ExperimentalSettingsImplementation::class, ExperimentalForeignApi::class)
    actual val settings: Settings?
        get() = KeychainSettings(
            kSecAttrService to CFBridgingRetain(KEYCHAIN_NAME),
            kSecAttrAccessible to kSecAttrAccessibleAlways
        )

    @OptIn(ExperimentalSettingsImplementation::class, ExperimentalForeignApi::class)
    actual val deprecatedSettings: Settings?
        get() = KeychainSettings(
            kSecAttrService to CFBridgingRetain(KEYCHAIN_NAME)
        )
}