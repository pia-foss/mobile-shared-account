package com.privateinternetaccess.account.internals.persistency.secureSettings

import com.privateinternetaccess.account.internals.Account
import com.privateinternetaccess.account.internals.model.response.ApiTokenResponse
import com.privateinternetaccess.account.internals.model.response.VpnTokenResponse
import com.privateinternetaccess.account.internals.persistency.AccountPersistence
import com.russhwolf.settings.Settings

internal object SecureSettingsPersistence : AccountPersistence {

    private val settings: Settings? = SecureSettingsProvider.settings

    override fun persistApiTokenResponse(apiToken: ApiTokenResponse) {
        settings?.putString(
            Account.API_TOKEN_KEY,
            Account.json.encodeToString(
                ApiTokenResponse.serializer(),
                apiToken
            )
        )
    }

    override fun persistVpnTokenResponse(vpnToken: VpnTokenResponse) {
        settings?.putString(
            Account.VPN_TOKEN_KEY,
            Account.json.encodeToString(
                VpnTokenResponse.serializer(),
                vpnToken
            )
        )
    }

    override fun apiTokenResponse(): ApiTokenResponse? =
        settings?.getStringOrNull(Account.API_TOKEN_KEY)?.let {
            Account.json.decodeFromString(ApiTokenResponse.serializer(), it)
        }

    override fun vpnTokenResponse(): VpnTokenResponse? =
        settings?.getStringOrNull(Account.VPN_TOKEN_KEY)?.let {
            Account.json.decodeFromString(VpnTokenResponse.serializer(), it)
        }

    override fun clearApiTokenResponse() {
        settings?.remove(Account.API_TOKEN_KEY)
    }

    override fun clearVpnTokenResponse() {
        settings?.remove(Account.VPN_TOKEN_KEY)
    }
}