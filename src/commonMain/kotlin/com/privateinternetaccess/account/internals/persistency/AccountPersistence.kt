package com.privateinternetaccess.account.internals.persistency

import com.privateinternetaccess.account.internals.model.response.ApiTokenResponse
import com.privateinternetaccess.account.internals.model.response.VpnTokenResponse

internal interface AccountPersistence {
    fun persistApiTokenResponse(apiToken: ApiTokenResponse)
    fun persistVpnTokenResponse(vpnToken: VpnTokenResponse)
    fun apiTokenResponse(): ApiTokenResponse?
    fun vpnTokenResponse(): VpnTokenResponse?
    fun clearApiTokenResponse()
    fun clearVpnTokenResponse()
}