package com.privateinternetaccess.account.internals.persistency.secureSettings

import com.russhwolf.settings.Settings

internal expect object SecureSettingsProvider {
    val settings: Settings?
}