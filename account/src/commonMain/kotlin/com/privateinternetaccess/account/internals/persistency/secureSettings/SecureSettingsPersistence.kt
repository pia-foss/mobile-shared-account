package com.privateinternetaccess.account.internals.persistency.secureSettings

import com.privateinternetaccess.account.internals.Account
import com.privateinternetaccess.account.internals.model.response.ApiTokenResponse
import com.privateinternetaccess.account.internals.model.response.VpnTokenResponse
import com.privateinternetaccess.account.internals.persistency.AccountPersistence
import com.russhwolf.settings.Settings

internal object SecureSettingsPersistence : AccountPersistence {

    private val settings: Settings? = SecureSettingsProvider.settings
    private val deprecatedSettings: Settings? = SecureSettingsProvider.deprecatedSettings

    override fun persistApiTokenResponse(apiToken: ApiTokenResponse) {
        migrateTokens()
        settings?.putString(
            Account.API_TOKEN_KEY,
            Account.json.encodeToString(
                ApiTokenResponse.serializer(),
                apiToken
            )
        )
    }

    override fun persistVpnTokenResponse(vpnToken: VpnTokenResponse) {
        migrateTokens()
        settings?.putString(
            Account.VPN_TOKEN_KEY,
            Account.json.encodeToString(
                VpnTokenResponse.serializer(),
                vpnToken
            )
        )
    }

    override fun apiTokenResponse(): ApiTokenResponse? {
        migrateTokens()
        return settings?.getStringOrNull(Account.API_TOKEN_KEY)?.let {
            Account.json.decodeFromString(ApiTokenResponse.serializer(), it)
        }
    }

    override fun vpnTokenResponse(): VpnTokenResponse? {
        migrateTokens()
        return settings?.getStringOrNull(Account.VPN_TOKEN_KEY)?.let {
            Account.json.decodeFromString(VpnTokenResponse.serializer(), it)
        }
    }

    override fun clearApiTokenResponse() {
        settings?.remove(Account.API_TOKEN_KEY)
        deprecatedSettings?.remove(Account.API_TOKEN_KEY)
    }

    override fun clearVpnTokenResponse() {
        settings?.remove(Account.VPN_TOKEN_KEY)
        deprecatedSettings?.remove(Account.VPN_TOKEN_KEY)
    }

    // region private
    private fun migrateTokens() {
        deprecatedSettings?.keys?.forEach {
            val value = deprecatedSettings.getString(it, "")
            deprecatedSettings.remove(it)
            settings?.putString(it, value)
        }
    }
    // endregion
}