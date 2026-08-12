package com.privateinternetaccess.account.internals.persistency.datastore

import com.privateinternetaccess.account.internals.Account
import com.privateinternetaccess.account.internals.model.response.ApiTokenResponse
import com.privateinternetaccess.account.internals.model.response.VpnTokenResponse
import com.privateinternetaccess.account.internals.persistency.AccountPersistence

internal object TokenPersistence : AccountPersistence {

    override suspend fun persistApiTokenResponse(apiToken: ApiTokenResponse) {
        EncryptedKeyValueStore.putString(
            Account.API_TOKEN_KEY,
            Account.json.encodeToString(ApiTokenResponse.serializer(), apiToken)
        )
    }

    override suspend fun persistVpnTokenResponse(vpnToken: VpnTokenResponse) {
        EncryptedKeyValueStore.putString(
            Account.VPN_TOKEN_KEY,
            Account.json.encodeToString(VpnTokenResponse.serializer(), vpnToken)
        )
    }

    override suspend fun apiTokenResponse(): ApiTokenResponse? =
        EncryptedKeyValueStore.getString(Account.API_TOKEN_KEY)?.let {
            Account.json.decodeFromString(ApiTokenResponse.serializer(), it)
        }

    override suspend fun vpnTokenResponse(): VpnTokenResponse? =
        EncryptedKeyValueStore.getString(Account.VPN_TOKEN_KEY)?.let {
            Account.json.decodeFromString(VpnTokenResponse.serializer(), it)
        }

    override suspend fun clearApiTokenResponse() {
        EncryptedKeyValueStore.remove(Account.API_TOKEN_KEY)
    }

    override suspend fun clearVpnTokenResponse() {
        EncryptedKeyValueStore.remove(Account.VPN_TOKEN_KEY)
    }

    override fun cachedApiTokenResponse(): ApiTokenResponse? =
        EncryptedKeyValueStore.cachedString(Account.API_TOKEN_KEY)?.let {
            Account.json.decodeFromString(ApiTokenResponse.serializer(), it)
        }

    override fun cachedVpnTokenResponse(): VpnTokenResponse? =
        EncryptedKeyValueStore.cachedString(Account.VPN_TOKEN_KEY)?.let {
            Account.json.decodeFromString(VpnTokenResponse.serializer(), it)
        }
}