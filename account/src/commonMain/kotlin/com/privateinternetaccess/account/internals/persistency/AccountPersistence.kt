package com.privateinternetaccess.account.internals.persistency

import com.privateinternetaccess.account.internals.model.response.ApiTokenResponse
import com.privateinternetaccess.account.internals.model.response.VpnTokenResponse

internal interface AccountPersistence {
    suspend fun persistApiTokenResponse(apiToken: ApiTokenResponse)
    suspend fun persistVpnTokenResponse(vpnToken: VpnTokenResponse)
    suspend fun apiTokenResponse(): ApiTokenResponse?
    suspend fun vpnTokenResponse(): VpnTokenResponse?
    suspend fun clearApiTokenResponse()
    suspend fun clearVpnTokenResponse()

    /**
     * Synchronous, in-memory-cache-backed reads for the non-suspend AccountAPI surface
     * (`Account.apiToken()` / `Account.vpnToken()`), which consuming apps call directly.
     */
    fun cachedApiTokenResponse(): ApiTokenResponse?
    fun cachedVpnTokenResponse(): VpnTokenResponse?
}