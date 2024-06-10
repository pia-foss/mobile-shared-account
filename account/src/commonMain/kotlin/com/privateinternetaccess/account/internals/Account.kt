/*
 *  Copyright (c) 2020 Private Internet Access, Inc.
 *
 *  This file is part of the Private Internet Access Mobile Client.
 *
 *  The Private Internet Access Mobile Client is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  The Private Internet Access Mobile Client is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with the Private
 *  Internet Access Mobile Client.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.privateinternetaccess.account.internals

import com.privateinternetaccess.account.*
import com.privateinternetaccess.account.internals.model.request.DedicatedIPRequest
import com.privateinternetaccess.account.internals.model.response.ApiTokenResponse
import com.privateinternetaccess.account.model.response.DipCountriesResponse
import com.privateinternetaccess.account.internals.model.response.SetEmailResponse
import com.privateinternetaccess.account.internals.model.response.VpnTokenResponse
import com.privateinternetaccess.account.internals.persistency.AccountPersistence
import com.privateinternetaccess.account.internals.persistency.secureSettings.SecureSettingsPersistence
import com.privateinternetaccess.account.internals.utils.AccountUtils
import com.privateinternetaccess.account.internals.utils.NetworkUtils.mapStatusCodeToAccountError
import com.privateinternetaccess.account.model.response.*
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse.DedicatedIPInformation
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.datetime.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime


internal expect object AccountHttpClient {

    /**
     * @param certificate String?. Certificate required for pinning capabilities.
     * @param pinnedEndpoint Pair<String, String>?. Contains endpoint as first, commonName as second.
     *
     * @return `Pair<HttpClient?, Exception?>`.
     *
     */
    fun client(
        certificate: String? = null,
        pinnedEndpoint: Pair<String, String>? = null,
        requestTimeoutMillis: Long = Account.REQUEST_TIMEOUT_MS
    ): Pair<HttpClient?, Exception?>
}


internal open class Account(
    internal val endpointsProvider: IAccountEndpointProvider,
    internal val certificate: String?,
    private val userAgentValue: String,
    private val platform: Platform,
    internal val persistence: AccountPersistence = SecureSettingsPersistence
) : CoroutineScope, AccountAPI {

    internal enum class Path(val url: String) {
        LOGIN("/api/client/v5/api_token"),
        VPN_TOKEN("/api/client/v5/vpn_token"),
        REFRESH_API_TOKEN("/api/client/v5/refresh"),
        ADDON_SIGNUP("/api/client/v5/dip_signup"),
        VPN_SIGNUP("/api/client/signup"),
        VPN_SIGNUP_AMAZON("/api/client/amazon/signup"),
        SET_EMAIL("/api/client/account"),
        LOGIN_LINK("/api/client/v2/login_link"),
        LOGOUT("/api/client/v2/expire_token"),
        ACCOUNT_DETAILS("/api/client/v2/account"),
        DELETE_ACCOUNT("/api/client/v5/account"),
        CLIENT_STATUS("/api/client/status"),
        INVITES("/api/client/invites"),
        REDEEM("/api/client/giftcard_redeem"),
        REFRESH_TOKEN("/api/client/v4/refresh"),
        MESSAGES("/api/client/v2/messages"),
        SUPPORTED_DEDICATED_IP_COUNTRIES("/api/client/v5/dip_regions"),
        REDEEM_DEDICATED_IP("/api/client/v2/dedicated_ip"),
        RENEW_DEDICATED_IP("/api/client/v2/check_renew_dip"),
        ANDROID_ADDONS_SUBSCRIPTIONS("/api/client/v5/android_addons"),
        ANDROID_VPN_SUBSCRIPTIONS("/api/client/android"),
        AMAZON_SUBSCRIPTIONS("/api/client/amazon"),
        ANDROID_FEATURE_FLAG("/clients/desktop/android-flags"),
        IOS_PAYMENT("/api/client/payment"),
        IOS_SUBSCRIPTIONS("/api/client/ios"),
        IOS_FEATURE_FLAG("/clients/desktop/ios-flags")
    }

    companion object {
        internal const val API_TOKEN_KEY = "API_TOKEN_KEY"
        internal const val VPN_TOKEN_KEY = "VPN_TOKEN_KEY"
        internal const val REQUEST_TIMEOUT_MS = 3000L
        internal const val MIN_EXPIRATION_THRESHOLD_DAYS = 21.0
        internal val json = Json { ignoreUnknownKeys = true; encodeDefaults = false }
        internal val SUBDOMAINS = mapOf(
            Path.LOGIN to "apiv5",
            Path.VPN_TOKEN to "apiv5",
            Path.REFRESH_API_TOKEN to "apiv5",
            Path.ADDON_SIGNUP to "apiv5",
            Path.VPN_SIGNUP to "api",
            Path.VPN_SIGNUP_AMAZON to "api",
            Path.SET_EMAIL to "api",
            Path.LOGIN_LINK to "apiv2",
            Path.LOGOUT to "apiv2",
            Path.ACCOUNT_DETAILS to "apiv2",
            Path.DELETE_ACCOUNT to "apiv5",
            Path.CLIENT_STATUS to "api",
            Path.INVITES to "api",
            Path.REDEEM to "api",
            Path.REFRESH_TOKEN to "apiv4",
            Path.MESSAGES to "apiv2",
            Path.SUPPORTED_DEDICATED_IP_COUNTRIES to "apiv5",
            Path.REDEEM_DEDICATED_IP to "apiv2",
            Path.RENEW_DEDICATED_IP to "apiv2",
            Path.ANDROID_ADDONS_SUBSCRIPTIONS to "apiv5",
            Path.ANDROID_VPN_SUBSCRIPTIONS to "api",
            Path.AMAZON_SUBSCRIPTIONS to "api",
            Path.ANDROID_FEATURE_FLAG to "api",
            Path.IOS_PAYMENT to "api",
            Path.IOS_SUBSCRIPTIONS to "api",
            Path.IOS_FEATURE_FLAG to "api",
        )
    }

    /**
     * Defines those requests going into the active requests pipeline while they are in progress.
     * To be re-evaluated once we have a dedicated background thread to run a selected list of requests sequentially.
     */
    private enum class RequestPipeline {
        API_TOKEN,
        VPN_TOKEN
    }

    /**
     * Pipeline containing all those requests of interest that are active at any moment.
     */
    private val requestsPipeline = mutableListOf<RequestPipeline>()

    // region CoroutineScope
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
    // endregion

    override fun apiToken(): String? {
        return persistence.apiTokenResponse()?.apiToken
    }

    override fun vpnToken(): String? {
        return persistence.vpnTokenResponse()?.let {
            "vpn_token_${it.vpnUsernameToken}:${it.vpnPasswordToken}"
        }
    }

    override fun migrateApiToken(
        apiToken: String,
        callback: (error: List<AccountRequestError>) -> Unit
    ) {
        launch {
            migrateApiTokenAsync(apiToken, endpointsProvider.accountEndpoints(), callback)
        }
    }

    override fun loginLink(email: String, callback: (error: List<AccountRequestError>) -> Unit) {
        launch {
            loginLinkAsync(email, endpointsProvider.accountEndpoints(), callback)
        }
    }

    override fun loginWithCredentials(
        username: String,
        password: String,
        callback: (error: List<AccountRequestError>) -> Unit
    ) {
        launch {
            loginWithCredentialsAsync(
                username,
                password,
                endpointsProvider.accountEndpoints(),
                callback
            )
        }
    }

    override fun logout(callback: (error: List<AccountRequestError>) -> Unit) {
        launch {
            logoutAsync(endpointsProvider.accountEndpoints(), callback)
        }
    }

    override fun accountDetails(callback: (details: AccountInformation?, error: List<AccountRequestError>) -> Unit) {
        launch {
            accountDetailsAsync(endpointsProvider.accountEndpoints(), callback)
        }
    }

    override fun deleteAccount(callback: (error: List<AccountRequestError>) -> Unit) {
        launch {
            deleteAccountAsync(endpointsProvider.accountEndpoints(), callback)
        }
    }

    override fun supportedDedicatedIPCountries(
        callback: (details: DipCountriesResponse?, error: List<AccountRequestError>) -> Unit
    ) {
        launch {
            supportedDedicatedIPCountriesAsync(endpointsProvider.accountEndpoints(), callback)
        }
    }

    override fun redeemDedicatedIPs(
        dipTokens: List<String>,
        callback: (details: List<DedicatedIPInformation>, error: List<AccountRequestError>) -> Unit
    ) {
        launch {
            redeemDedicatedIPsAsync(dipTokens, endpointsProvider.accountEndpoints(), callback)
        }
    }

    override fun renewDedicatedIP(
        dipToken: String,
        callback: (error: List<AccountRequestError>) -> Unit
    ) {
        launch {
            renewDedicatedIPAsync(dipToken, endpointsProvider.accountEndpoints(), callback)
        }
    }

    override fun clientStatus(
        requestTimeoutMillis: Long,
        callback: (status: ClientStatusInformation?, error: List<AccountRequestError>) -> Unit

    ) {
        launch {
            clientStatusAsync(endpointsProvider.accountEndpoints(), callback, requestTimeoutMillis)
        }
    }

    override fun setEmail(
        email: String,
        resetPassword: Boolean,
        callback: (temporaryPassword: String?, error: List<AccountRequestError>) -> Unit
    ) {
        launch {
            setEmailAsync(email, resetPassword, endpointsProvider.accountEndpoints(), callback)
        }
    }

    override fun sendInvite(
        recipientEmail: String,
        recipientName: String,
        callback: (error: List<AccountRequestError>) -> Unit
    ) {
        launch {
            sendInviteAsync(
                recipientEmail,
                recipientName,
                endpointsProvider.accountEndpoints(),
                callback
            )
        }
    }

    override fun invitesDetails(
        callback: (details: InvitesDetailsInformation?, error: List<AccountRequestError>) -> Unit
    ) {
        launch {
            invitesDetailsAsync(endpointsProvider.accountEndpoints(), callback)
        }
    }

    override fun redeem(
        email: String,
        code: String,
        callback: (details: RedeemInformation?, error: List<AccountRequestError>) -> Unit
    ) {
        launch {
            redeemAsync(email, code, endpointsProvider.accountEndpoints(), callback)
        }
    }

    override fun message(
        appVersion: String,
        callback: (message: MessageInformation?, error: List<AccountRequestError>) -> Unit
    ) {
        launch {
            messageAsync(appVersion, endpointsProvider.accountEndpoints(), callback)
        }
    }

    override fun featureFlags(
        callback: (details: FeatureFlagsInformation?, error: List<AccountRequestError>) -> Unit
    ) {
        launch {
            featureFlagsAsync(endpointsProvider.accountEndpoints(), callback)
        }
    }
    // endregion

    // region private
    private suspend fun migrateApiTokenAsync(
        apiToken: String,
        endpoints: List<AccountEndpoint>,
        callback: (error: List<AccountRequestError>) -> Unit
    ) {
        val listErrors: MutableList<AccountRequestError> = mutableListOf()
        if (endpoints.isEmpty()) {
            listErrors.add(
                AccountRequestError(
                    600,
                    "No available endpoints to perform the request"
                )
            )
        }

        listErrors.addAll(refreshApiToken(apiToken, endpoints))
        //If we succeeded Get a VPN token as well
        apiToken()?.let {
            listErrors.addAll(refreshVpnToken(it, endpoints))
        }

        withContext(Dispatchers.Main) {
            callback(listErrors)
        }
    }

    private suspend fun loginLinkAsync(
        email: String,
        endpoints: List<AccountEndpoint>,
        callback: (error: List<AccountRequestError>) -> Unit
    ) {
        val listErrors: MutableList<AccountRequestError> = mutableListOf()
        if (endpoints.isEmpty()) {
            listErrors.add(
                AccountRequestError(
                    600,
                    "No available endpoints to perform the request"
                )
            )
        }

        for (endpoint in endpoints) {
            if (endpoint.usePinnedCertificate && certificate.isNullOrEmpty()) {
                listErrors.add(
                    AccountRequestError(
                        600,
                        "No available certificate for pinning purposes"
                    )
                )
                continue
            }

            val httpClientConfigResult = if (endpoint.usePinnedCertificate) {
                AccountHttpClient.client(certificate, Pair(endpoint.ipOrRootDomain, endpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }

            val httpClient = httpClientConfigResult.first
            val httpClientError = httpClientConfigResult.second
            if (httpClientError != null) {
                listErrors.add(AccountRequestError(600, httpClientError.message))
                continue
            }

            if (httpClient == null) {
                listErrors.add(AccountRequestError(600, "Invalid http client"))
                continue
            }

            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, Path.LOGIN_LINK)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${Path.LOGIN_LINK.url}"))
                continue
            }

            var succeeded = false
            val formParameters = Parameters.build {
                append("email", email)
            }
            val response = httpClient.postCatching<Pair<HttpResponse?, Exception?>>(formParameters = formParameters) {
                url(url)
            }

            response.first?.let {
                succeeded = AccountUtils.isErrorStatusCode(it.status.value).not()
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(it.mapStatusCodeToAccountError())
                }
            }
            response.second?.let {
                listErrors.add(AccountRequestError(600, it.message))
            }

            // Close the used client explicitly.
            // We need to recreate it due to the possibility of pinning among the endpoints list.
            httpClient.close()

            // If there were no errors in the request for the current endpoint. No need to try the next endpoint.
            if (succeeded) {
                listErrors.clear()
                break
            }
        }

        withContext(Dispatchers.Main) {
            callback(listErrors)
        }
    }

    private suspend fun loginWithCredentialsAsync(
        username: String,
        password: String,
        endpoints: List<AccountEndpoint>,
        callback: (error: List<AccountRequestError>) -> Unit
    ) {
        val listErrors: MutableList<AccountRequestError> = mutableListOf()
        if (endpoints.isEmpty()) {
            listErrors.add(
                AccountRequestError(
                    600,
                    "No available endpoints to perform the request"
                )
            )
        }

        for (endpoint in endpoints) {
            if (endpoint.usePinnedCertificate && certificate.isNullOrEmpty()) {
                listErrors.add(
                    AccountRequestError(
                        600,
                        "No available certificate for pinning purposes"
                    )
                )
                continue
            }

            val httpClientConfigResult = if (endpoint.usePinnedCertificate) {
                AccountHttpClient.client(certificate, Pair(endpoint.ipOrRootDomain, endpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }

            val httpClient = httpClientConfigResult.first
            val httpClientError = httpClientConfigResult.second
            if (httpClientError != null) {
                listErrors.add(AccountRequestError(600, httpClientError.message))
                continue
            }

            if (httpClient == null) {
                listErrors.add(AccountRequestError(600, "Invalid http client"))
                continue
            }

            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, Path.LOGIN)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${Path.LOGIN.url}"))
                continue
            }

            var succeeded = false
            val formParameters = Parameters.build {
                append("username", username)
                append("password", password)
            }
            val requestResponse = httpClient.postCatching<Pair<HttpResponse?, Exception?>>(formParameters = formParameters) {
                url(url)
            }

            requestResponse.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(it.mapStatusCodeToAccountError())
                } else {
                    try {
                        val apiTokenResponse = json.decodeFromString(ApiTokenResponse.serializer(), it.bodyAsText())
                        persistence.persistApiTokenResponse(apiTokenResponse)
                        refreshVpnToken(apiTokenResponse.apiToken, endpoints)
                        succeeded = true
                    } catch (exception: SerializationException) {
                        listErrors.add(AccountRequestError(600, "Decode error $exception"))
                    }
                }
            }
            requestResponse.second?.let {
                listErrors.add(AccountRequestError(600, it.message))
            }

            // Close the used client explicitly.
            // We need to recreate it due to the possibility of pinning among the endpoints list.
            httpClient.close()

            // If there were no errors in the request for the current endpoint. No need to try the next endpoint.
            if (succeeded) {
                listErrors.clear()
                break
            }
        }

        withContext(Dispatchers.Main) {
            callback(listErrors)
        }
    }

    private suspend fun logoutAsync(
        endpoints: List<AccountEndpoint>,
        callback: (error: List<AccountRequestError>) -> Unit
    ) {
        val listErrors: MutableList<AccountRequestError> = mutableListOf()
        if (endpoints.isEmpty()) {
            listErrors.add(
                AccountRequestError(
                    600,
                    "No available endpoints to perform the request"
                )
            )
        }

        refreshTokensIfNeeded(endpoints)
        for (endpoint in endpoints) {
            val apiToken = persistence.apiTokenResponse()?.apiToken
            if (apiToken == null) {
                listErrors.add(AccountRequestError(600, "Invalid request token"))
                break
            }

            if (endpoint.usePinnedCertificate && certificate.isNullOrEmpty()) {
                listErrors.add(
                    AccountRequestError(
                        600,
                        "No available certificate for pinning purposes"
                    )
                )
                continue
            }

            val httpClientConfigResult = if (endpoint.usePinnedCertificate) {
                AccountHttpClient.client(certificate, Pair(endpoint.ipOrRootDomain, endpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }

            val httpClient = httpClientConfigResult.first
            val httpClientError = httpClientConfigResult.second
            if (httpClientError != null) {
                listErrors.add(AccountRequestError(600, httpClientError.message))
                continue
            }

            if (httpClient == null) {
                listErrors.add(AccountRequestError(600, "Invalid http client"))
                continue
            }

            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, Path.LOGOUT)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${Path.LOGOUT.url}"))
                continue
            }

            var succeeded = false
            val response = httpClient.postCatching<Pair<HttpResponse?, Exception?>> {
                url(url)
                header("Authorization", "Token $apiToken")
            }

            response.first?.let {
                succeeded = AccountUtils.isErrorStatusCode(it.status.value).not()
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(AccountRequestError(it.status.value, it.status.description))
                }
            }
            response.second?.let {
                listErrors.add(AccountRequestError(600, it.message))
            }

            // Close the used client explicitly.
            // We need to recreate it due to the possibility of pinning among the endpoints list.
            httpClient.close()

            // If there were no errors in the request for the current endpoint. No need to try the next endpoint.
            if (succeeded) {
                listErrors.clear()
                break
            }
        }

        // Regardless of the request result. The client has stated we are logging out. Wiped the persisted tokens.
        persistence.clearApiTokenResponse()
        persistence.clearVpnTokenResponse()

        withContext(Dispatchers.Main) {
            callback(listErrors)
        }
    }

    private suspend fun accountDetailsAsync(
        endpoints: List<AccountEndpoint>,
        callback: (details: AccountInformation?, error: List<AccountRequestError>) -> Unit
    ) {
        var accountInformation: AccountInformation? = null
        val listErrors: MutableList<AccountRequestError> = mutableListOf()
        if (endpoints.isEmpty()) {
            listErrors.add(
                AccountRequestError(
                    600,
                    "No available endpoints to perform the request"
                )
            )
        }

        refreshTokensIfNeeded(endpoints)
        for (endpoint in endpoints) {
            val apiToken = persistence.apiTokenResponse()?.apiToken
            if (apiToken == null) {
                listErrors.add(AccountRequestError(600, "Invalid request token"))
                break
            }

            if (endpoint.usePinnedCertificate && certificate.isNullOrEmpty()) {
                listErrors.add(
                    AccountRequestError(
                        600,
                        "No available certificate for pinning purposes"
                    )
                )
                continue
            }

            val httpClientConfigResult = if (endpoint.usePinnedCertificate) {
                AccountHttpClient.client(certificate, Pair(endpoint.ipOrRootDomain, endpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }

            val httpClient = httpClientConfigResult.first
            val httpClientError = httpClientConfigResult.second
            if (httpClientError != null) {
                listErrors.add(AccountRequestError(600, httpClientError.message))
                continue
            }

            if (httpClient == null) {
                listErrors.add(AccountRequestError(600, "Invalid http client"))
                continue
            }

            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, Path.ACCOUNT_DETAILS)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${Path.ACCOUNT_DETAILS.url}"))
                continue
            }

            var succeeded = false
            val response = httpClient.getCatching<Pair<HttpResponse?, Exception?>> {
                url(url)
                header("Authorization", "Token $apiToken")
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(AccountRequestError(it.status.value, it.status.description))
                } else {
                    try {
                        accountInformation = json.decodeFromString(AccountInformation.serializer(), it.bodyAsText())
                        succeeded = true
                    } catch (exception: SerializationException) {
                        listErrors.add(AccountRequestError(600, "Decode error $exception"))
                    }
                }
            }
            response.second?.let {
                listErrors.add(AccountRequestError(600, it.message))
            }

            // Close the used client explicitly.
            // We need to recreate it due to the possibility of pinning among the endpoints list.
            httpClient.close()

            // If there were no errors in the request for the current endpoint. No need to try the next endpoint.
            if (succeeded) {
                listErrors.clear()
                break
            }
        }

        withContext(Dispatchers.Main) {
            callback(accountInformation, listErrors)
        }
    }

    private suspend fun deleteAccountAsync(
        endpoints: List<AccountEndpoint>,
        callback: (error: List<AccountRequestError>) -> Unit
    ) {
        val listErrors: MutableList<AccountRequestError> = mutableListOf()
        if (endpoints.isEmpty()) {
            listErrors.add(
                AccountRequestError(
                    600,
                    "No available endpoints to perform the request"
                )
            )
        }

        refreshTokensIfNeeded(endpoints)
        for (endpoint in endpoints) {
            val apiToken = persistence.apiTokenResponse()?.apiToken
            if (apiToken == null) {
                listErrors.add(AccountRequestError(600, "Invalid request token"))
                break
            }

            if (endpoint.usePinnedCertificate && certificate.isNullOrEmpty()) {
                listErrors.add(
                    AccountRequestError(
                        600,
                        "No available certificate for pinning purposes"
                    )
                )
                continue
            }

            val httpClientConfigResult = if (endpoint.usePinnedCertificate) {
                AccountHttpClient.client(certificate, Pair(endpoint.ipOrRootDomain, endpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }

            val httpClient = httpClientConfigResult.first
            val httpClientError = httpClientConfigResult.second
            if (httpClientError != null) {
                listErrors.add(AccountRequestError(600, httpClientError.message))
                continue
            }

            if (httpClient == null) {
                listErrors.add(AccountRequestError(600, "Invalid http client"))
                continue
            }

            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, Path.DELETE_ACCOUNT)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${Path.DELETE_ACCOUNT.url}"))
                continue
            }

            var succeeded = false
            val response = httpClient.deleteCatching<Pair<HttpResponse?, Exception?>> {
                url(url)
                header("Authorization", "Token $apiToken")
            }

            response.first?.let {
                succeeded = AccountUtils.isErrorStatusCode(it.status.value).not()
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(AccountRequestError(it.status.value, it.status.description))
                }
            }
            response.second?.let {
                listErrors.add(AccountRequestError(600, it.message))
            }

            // Close the used client explicitly.
            // We need to recreate it due to the possibility of pinning among the endpoints list.
            httpClient.close()

            // If there were no errors in the request for the current endpoint. No need to try the next endpoint.
            if (succeeded) {
                listErrors.clear()
                // If we are receiving a 200, we should remove the persisted tokens for the client as the account has been now deleted.
                persistence.clearApiTokenResponse()
                persistence.clearVpnTokenResponse()
                break
            }
        }

        withContext(Dispatchers.Main) {
            callback(listErrors)
        }
    }

    private suspend fun supportedDedicatedIPCountriesAsync(
        endpoints: List<AccountEndpoint>,
        callback: (details: DipCountriesResponse?, error: List<AccountRequestError>) -> Unit
    ) {
        val listErrors: MutableList<AccountRequestError> = mutableListOf()
        var supportedDedicatedIPsCountries: DipCountriesResponse? = null
        if (endpoints.isEmpty()) {
            listErrors.add(
                AccountRequestError(
                    600,
                    "No available endpoints to perform the request"
                )
            )
        }

        refreshTokensIfNeeded(endpoints)
        for (endpoint in endpoints) {
            val apiToken = persistence.apiTokenResponse()?.apiToken
            if (apiToken == null) {
                listErrors.add(AccountRequestError(600, "Invalid request token"))
                break
            }

            if (endpoint.usePinnedCertificate && certificate.isNullOrEmpty()) {
                listErrors.add(
                    AccountRequestError(
                        600,
                        "No available certificate for pinning purposes"
                    )
                )
                continue
            }

            val httpClientConfigResult = if (endpoint.usePinnedCertificate) {
                AccountHttpClient.client(certificate, Pair(endpoint.ipOrRootDomain, endpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }

            val httpClient = httpClientConfigResult.first
            val httpClientError = httpClientConfigResult.second
            if (httpClientError != null) {
                listErrors.add(AccountRequestError(600, httpClientError.message))
                continue
            }

            if (httpClient == null) {
                listErrors.add(AccountRequestError(600, "Invalid http client"))
                continue
            }

            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, Path.SUPPORTED_DEDICATED_IP_COUNTRIES)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${Path.SUPPORTED_DEDICATED_IP_COUNTRIES.url}"))
                continue
            }

            var succeeded = false
            val response = httpClient.getCatching<Pair<HttpResponse?, Exception?>> {
                url(url)
                header("Authorization", "Token $apiToken")
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(it.mapStatusCodeToAccountError())
                } else {
                    try {
                        supportedDedicatedIPsCountries = json.decodeFromString(
                            DipCountriesResponse.serializer(), it.bodyAsText()
                        )
                        succeeded = true
                    } catch (exception: SerializationException) {
                        listErrors.add(AccountRequestError(600, "Decode error $exception"))
                    }
                }
            }
            response.second?.let {
                listErrors.add(AccountRequestError(600, it.message))
            }

            // Close the used client explicitly.
            // We need to recreate it due to the possibility of pinning among the endpoints list.
            httpClient.close()

            // If there were no errors in the request for the current endpoint. No need to try the next endpoint.
            if (succeeded) {
                listErrors.clear()
                break
            }
        }

        withContext(Dispatchers.Main) {
            callback(supportedDedicatedIPsCountries, listErrors)
        }
    }

    private suspend fun redeemDedicatedIPsAsync(
        dipTokens: List<String>,
        endpoints: List<AccountEndpoint>,
        callback: (details: List<DedicatedIPInformation>, error: List<AccountRequestError>) -> Unit
    ) {
        val listErrors: MutableList<AccountRequestError> = mutableListOf()
        var dedicatedIPsInformation: List<DedicatedIPInformation> = emptyList()
        if (endpoints.isEmpty()) {
            listErrors.add(
                AccountRequestError(
                    600,
                    "No available endpoints to perform the request"
                )
            )
        }

        refreshTokensIfNeeded(endpoints)
        for (endpoint in endpoints) {
            val apiToken = persistence.apiTokenResponse()?.apiToken
            if (apiToken == null) {
                listErrors.add(AccountRequestError(600, "Invalid request token"))
                break
            }

            if (endpoint.usePinnedCertificate && certificate.isNullOrEmpty()) {
                listErrors.add(
                    AccountRequestError(
                        600,
                        "No available certificate for pinning purposes"
                    )
                )
                continue
            }

            val httpClientConfigResult = if (endpoint.usePinnedCertificate) {
                AccountHttpClient.client(certificate, Pair(endpoint.ipOrRootDomain, endpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }

            val httpClient = httpClientConfigResult.first
            val httpClientError = httpClientConfigResult.second
            if (httpClientError != null) {
                listErrors.add(AccountRequestError(600, httpClientError.message))
                continue
            }

            if (httpClient == null) {
                listErrors.add(AccountRequestError(600, "Invalid http client"))
                continue
            }

            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, Path.REDEEM_DEDICATED_IP)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${Path.REDEEM_DEDICATED_IP.url}"))
                continue
            }

            var succeeded = false
            val response = httpClient.postCatching<Pair<HttpResponse?, Exception?>> {
                url(url)
                header("Authorization", "Token $apiToken")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(DedicatedIPRequest.serializer(), DedicatedIPRequest(dipTokens)))
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(it.mapStatusCodeToAccountError())
                } else {
                    try {
                        dedicatedIPsInformation =
                            json.decodeFromString(
                                DedicatedIPInformationResponse.serializer(), "{\"result\":${it.bodyAsText()}}"
                            ).result
                        succeeded = true
                    } catch (exception: SerializationException) {
                        listErrors.add(AccountRequestError(600, "Decode error $exception"))
                    }
                }
            }
            response.second?.let {
                listErrors.add(AccountRequestError(600, it.message))
            }

            // Close the used client explicitly.
            // We need to recreate it due to the possibility of pinning among the endpoints list.
            httpClient.close()

            // If there were no errors in the request for the current endpoint. No need to try the next endpoint.
            if (succeeded) {
                listErrors.clear()
                break
            }
        }

        withContext(Dispatchers.Main) {
            callback(dedicatedIPsInformation, listErrors)
        }
    }

    private suspend fun renewDedicatedIPAsync(
        dipToken: String,
        endpoints: List<AccountEndpoint>,
        callback: (error: List<AccountRequestError>) -> Unit
    ) {
        val listErrors: MutableList<AccountRequestError> = mutableListOf()
        if (endpoints.isEmpty()) {
            listErrors.add(
                AccountRequestError(
                    600,
                    "No available endpoints to perform the request"
                )
            )
        }

        refreshTokensIfNeeded(endpoints)
        for (endpoint in endpoints) {
            val apiToken = persistence.apiTokenResponse()?.apiToken
            if (apiToken == null) {
                listErrors.add(AccountRequestError(600, "Invalid request token"))
                break
            }

            if (endpoint.usePinnedCertificate && certificate.isNullOrEmpty()) {
                listErrors.add(
                    AccountRequestError(
                        600,
                        "No available certificate for pinning purposes"
                    )
                )
                continue
            }

            val httpClientConfigResult = if (endpoint.usePinnedCertificate) {
                AccountHttpClient.client(certificate, Pair(endpoint.ipOrRootDomain, endpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }

            val httpClient = httpClientConfigResult.first
            val httpClientError = httpClientConfigResult.second
            if (httpClientError != null) {
                listErrors.add(AccountRequestError(600, httpClientError.message))
                continue
            }

            if (httpClient == null) {
                listErrors.add(AccountRequestError(600, "Invalid http client"))
                continue
            }

            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, Path.RENEW_DEDICATED_IP)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${Path.RENEW_DEDICATED_IP.url}"))
                continue
            }

            var succeeded = false
            val formParameters = Parameters.build {
                append("token", dipToken)
            }
            val response = httpClient.postCatching<Pair<HttpResponse?, Exception?>>(formParameters = formParameters) {
                url(url)
                header("Authorization", "Token $apiToken")
            }

            response.first?.let {
                succeeded = AccountUtils.isErrorStatusCode(it.status.value).not()
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(AccountRequestError(it.status.value, it.status.description))
                }
            }
            response.second?.let {
                listErrors.add(AccountRequestError(600, it.message))
            }

            // Close the used client explicitly.
            // We need to recreate it due to the possibility of pinning among the endpoints list.
            httpClient.close()

            // If there were no errors in the request for the current endpoint. No need to try the next endpoint.
            if (succeeded) {
                listErrors.clear()
                break
            }
        }

        withContext(Dispatchers.Main) {
            callback(listErrors)
        }
    }

    private suspend fun clientStatusAsync(
        endpoints: List<AccountEndpoint>,
        callback: (status: ClientStatusInformation?, error: List<AccountRequestError>) -> Unit,
        requestTimeoutMillis: Long
    ) {
        var clientStatus: ClientStatusInformation? = null
        val listErrors: MutableList<AccountRequestError> = mutableListOf()
        val filteredOutProxies = endpoints.filterNot { it.isProxy }
        if (filteredOutProxies.isEmpty()) {
            listErrors.add(
                AccountRequestError(
                    600,
                    "No available endpoints to perform the request"
                )
            )
        }

        refreshTokensIfNeeded(endpoints)
        for (endpoint in filteredOutProxies) {
            if (endpoint.usePinnedCertificate && certificate.isNullOrEmpty()) {
                listErrors.add(
                    AccountRequestError(
                        600,
                        "No available certificate for pinning purposes"
                    )
                )
                continue
            }

            val httpClientConfigResult = if (endpoint.usePinnedCertificate) {
                AccountHttpClient.client(certificate, Pair(endpoint.ipOrRootDomain, endpoint.certificateCommonName!!), requestTimeoutMillis)
            } else {
                AccountHttpClient.client(requestTimeoutMillis = requestTimeoutMillis)
            }

            val httpClient = httpClientConfigResult.first
            val httpClientError = httpClientConfigResult.second
            if (httpClientError != null) {
                listErrors.add(AccountRequestError(600, httpClientError.message))
                continue
            }

            if (httpClient == null) {
                listErrors.add(AccountRequestError(600, "Invalid http client"))
                continue
            }

            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, Path.CLIENT_STATUS)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${Path.CLIENT_STATUS.url}"))
                continue
            }

            var succeeded = false
            val response = httpClient.getCatching<Pair<HttpResponse?, Exception?>> {
                url(url)
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(AccountRequestError(it.status.value, it.status.description))
                } else {
                    try {
                        clientStatus = json.decodeFromString(ClientStatusInformation.serializer(), it.bodyAsText())
                        succeeded = true
                    } catch (exception: SerializationException) {
                        listErrors.add(AccountRequestError(600, "Decode error $exception"))
                    }
                }
            }
            response.second?.let {
                listErrors.add(AccountRequestError(600, it.message))
            }

            // Close the used client explicitly.
            // We need to recreate it due to the possibility of pinning among the endpoints list.
            httpClient.close()

            // If there were no errors in the request for the current endpoint. No need to try the next endpoint.
            if (succeeded) {
                listErrors.clear()
                break
            }
        }

        withContext(Dispatchers.Main) {
            callback(clientStatus, listErrors)
        }
    }

    private suspend fun setEmailAsync(
        email: String,
        resetPassword: Boolean,
        endpoints: List<AccountEndpoint>,
        callback: (temporaryPassword: String?, error: List<AccountRequestError>) -> Unit
    ) {
        var temporaryPassword: String? = null
        val listErrors: MutableList<AccountRequestError> = mutableListOf()
        if (endpoints.isEmpty()) {
            listErrors.add(
                AccountRequestError(
                    600,
                    "No available endpoints to perform the request"
                )
            )
        }

        refreshTokensIfNeeded(endpoints)
        for (endpoint in endpoints) {
            val apiToken = persistence.apiTokenResponse()?.apiToken
            if (apiToken == null) {
                listErrors.add(AccountRequestError(600, "Invalid request token"))
                break
            }

            if (endpoint.usePinnedCertificate && certificate.isNullOrEmpty()) {
                listErrors.add(
                    AccountRequestError(
                        600,
                        "No available certificate for pinning purposes"
                    )
                )
                continue
            }

            val httpClientConfigResult = if (endpoint.usePinnedCertificate) {
                AccountHttpClient.client(certificate, Pair(endpoint.ipOrRootDomain, endpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }

            val httpClient = httpClientConfigResult.first
            val httpClientError = httpClientConfigResult.second
            if (httpClientError != null) {
                listErrors.add(AccountRequestError(600, httpClientError.message))
                continue
            }

            if (httpClient == null) {
                listErrors.add(AccountRequestError(600, "Invalid http client"))
                continue
            }

            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, Path.SET_EMAIL)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${Path.SET_EMAIL.url}"))
                continue
            }

            var succeeded = false
            val formParameters = Parameters.build {
                append("email", email)
                append("reset_password", resetPassword.toString())
            }
            val response = httpClient.postCatching<Pair<HttpResponse?, Exception?>>(formParameters = formParameters) {
                url(url)
                header("Authorization", "Token $apiToken")
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(AccountRequestError(it.status.value, it.status.description))
                } else {
                    try {
                        temporaryPassword = json.decodeFromString(SetEmailResponse.serializer(), it.bodyAsText()).password
                        succeeded = true
                    } catch (exception: SerializationException) {
                        listErrors.add(AccountRequestError(600, "Decode error $exception"))
                    }
                }
            }
            response.second?.let {
                listErrors.add(AccountRequestError(600, it.message))
            }

            // Close the used client explicitly.
            // We need to recreate it due to the possibility of pinning among the endpoints list.
            httpClient.close()

            // If there were no errors in the request for the current endpoint. No need to try the next endpoint.
            if (succeeded) {
                listErrors.clear()
                break
            }
        }

        withContext(Dispatchers.Main) {
            callback(temporaryPassword, listErrors)
        }
    }

    private suspend fun sendInviteAsync(
        recipientEmail: String,
        recipientName: String,
        endpoints: List<AccountEndpoint>,
        callback: (error: List<AccountRequestError>) -> Unit
    ) {
        val listErrors: MutableList<AccountRequestError> = mutableListOf()
        if (endpoints.isEmpty()) {
            listErrors.add(
                AccountRequestError(
                    600,
                    "No available endpoints to perform the request"
                )
            )
        }

        refreshTokensIfNeeded(endpoints)
        for (endpoint in endpoints) {
            val apiToken = persistence.apiTokenResponse()?.apiToken
            if (apiToken == null) {
                listErrors.add(AccountRequestError(600, "Invalid request token"))
                break
            }

            if (endpoint.usePinnedCertificate && certificate.isNullOrEmpty()) {
                listErrors.add(
                    AccountRequestError(
                        600,
                        "No available certificate for pinning purposes"
                    )
                )
                continue
            }

            val httpClientConfigResult = if (endpoint.usePinnedCertificate) {
                AccountHttpClient.client(certificate, Pair(endpoint.ipOrRootDomain, endpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }

            val httpClient = httpClientConfigResult.first
            val httpClientError = httpClientConfigResult.second
            if (httpClientError != null) {
                listErrors.add(AccountRequestError(600, httpClientError.message))
                continue
            }

            if (httpClient == null) {
                listErrors.add(AccountRequestError(600, "Invalid http client"))
                continue
            }

            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, Path.INVITES)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${Path.INVITES.url}"))
                continue
            }

            var succeeded = false
            val formParameters = Parameters.build {
                append("invitee_email", recipientEmail)
                append("invitee_name", recipientName)
            }
            val response = httpClient.postCatching<Pair<HttpResponse?, Exception?>>(formParameters = formParameters) {
                url(url)
                header("Authorization", "Token $apiToken")
            }

            response.first?.let {
                succeeded = AccountUtils.isErrorStatusCode(it.status.value).not()
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(AccountRequestError(it.status.value, it.status.description))
                }
            }
            response.second?.let {
                listErrors.add(AccountRequestError(600, it.message))
            }

            // Close the used client explicitly.
            // We need to recreate it due to the possibility of pinning among the endpoints list.
            httpClient.close()

            // If there were no errors in the request for the current endpoint. No need to try the next endpoint.
            if (succeeded) {
                listErrors.clear()
                break
            }
        }

        withContext(Dispatchers.Main) {
            callback(listErrors)
        }
    }

    private suspend fun invitesDetailsAsync(
        endpoints: List<AccountEndpoint>,
        callback: (details: InvitesDetailsInformation?, error: List<AccountRequestError>) -> Unit
    ) {
        var invitesDetailsInformation: InvitesDetailsInformation? = null
        val listErrors: MutableList<AccountRequestError> = mutableListOf()
        if (endpoints.isEmpty()) {
            listErrors.add(
                AccountRequestError(
                    600,
                    "No available endpoints to perform the request"
                )
            )
        }

        refreshTokensIfNeeded(endpoints)
        for (endpoint in endpoints) {
            val apiToken = persistence.apiTokenResponse()?.apiToken
            if (apiToken == null) {
                listErrors.add(AccountRequestError(600, "Invalid request token"))
                break
            }

            if (endpoint.usePinnedCertificate && certificate.isNullOrEmpty()) {
                listErrors.add(
                    AccountRequestError(
                        600,
                        "No available certificate for pinning purposes"
                    )
                )
                continue
            }

            val httpClientConfigResult = if (endpoint.usePinnedCertificate) {
                AccountHttpClient.client(certificate, Pair(endpoint.ipOrRootDomain, endpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }

            val httpClient = httpClientConfigResult.first
            val httpClientError = httpClientConfigResult.second
            if (httpClientError != null) {
                listErrors.add(AccountRequestError(600, httpClientError.message))
                continue
            }

            if (httpClient == null) {
                listErrors.add(AccountRequestError(600, "Invalid http client"))
                continue
            }

            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, Path.INVITES)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${Path.INVITES.url}"))
                continue
            }

            var succeeded = false
            val response = httpClient.getCatching<Pair<HttpResponse?, Exception?>> {
                url(url)
                header("Authorization", "Token $apiToken")
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(AccountRequestError(it.status.value, it.status.description))
                } else {
                    try {
                        invitesDetailsInformation = json.decodeFromString(InvitesDetailsInformation.serializer(), it.bodyAsText())
                        succeeded = true
                    } catch (exception: SerializationException) {
                        listErrors.add(AccountRequestError(600, "Decode error $exception"))
                    }
                }
            }
            response.second?.let {
                listErrors.add(AccountRequestError(600, it.message))
            }

            // Close the used client explicitly.
            // We need to recreate it due to the possibility of pinning among the endpoints list.
            httpClient.close()

            // If there were no errors in the request for the current endpoint. No need to try the next endpoint.
            if (succeeded) {
                listErrors.clear()
                break
            }
        }

        withContext(Dispatchers.Main) {
            callback(invitesDetailsInformation, listErrors)
        }
    }

    private suspend fun redeemAsync(
        email: String,
        code: String,
        endpoints: List<AccountEndpoint>,
        callback: (details: RedeemInformation?, error: List<AccountRequestError>) -> Unit
    ) {
        var redeemInformation: RedeemInformation? = null
        val listErrors: MutableList<AccountRequestError> = mutableListOf()
        if (endpoints.isEmpty()) {
            listErrors.add(
                AccountRequestError(
                    600,
                    "No available endpoints to perform the request"
                )
            )
        }

        refreshTokensIfNeeded(endpoints)
        for (endpoint in endpoints) {
            if (endpoint.usePinnedCertificate && certificate.isNullOrEmpty()) {
                listErrors.add(
                    AccountRequestError(
                        600,
                        "No available certificate for pinning purposes"
                    )
                )
                continue
            }

            val httpClientConfigResult = if (endpoint.usePinnedCertificate) {
                AccountHttpClient.client(certificate, Pair(endpoint.ipOrRootDomain, endpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }

            val httpClient = httpClientConfigResult.first
            val httpClientError = httpClientConfigResult.second
            if (httpClientError != null) {
                listErrors.add(AccountRequestError(600, httpClientError.message))
                continue
            }

            if (httpClient == null) {
                listErrors.add(AccountRequestError(600, "Invalid http client"))
                continue
            }

            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, Path.REDEEM)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${Path.REDEEM.url}"))
                continue
            }

            var succeeded = false
            val formParameters = Parameters.build {
                append("email", email)
                append("pin", code)
            }
            val response = httpClient.postCatching<Pair<HttpResponse?, Exception?>>(formParameters = formParameters) {
                url(url)
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(AccountRequestError(it.status.value, it.status.description))
                } else {
                    try {
                        redeemInformation = json.decodeFromString(RedeemInformation.serializer(), it.bodyAsText())
                        succeeded = true
                    } catch (exception: SerializationException) {
                        listErrors.add(AccountRequestError(600, "Decode error $exception"))
                    }
                }
            }
            response.second?.let {
                listErrors.add(AccountRequestError(600, it.message))
            }

            // Close the used client explicitly.
            // We need to recreate it due to the possibility of pinning among the endpoints list.
            httpClient.close()

            // If there were no errors in the request for the current endpoint. No need to try the next endpoint.
            if (succeeded) {
                listErrors.clear()
                break
            }
        }

        withContext(Dispatchers.Main) {
            callback(redeemInformation, listErrors)
        }
    }

    private suspend fun messageAsync(
        appVersion: String,
        endpoints: List<AccountEndpoint>,
        callback: (message: MessageInformation?, error: List<AccountRequestError>) -> Unit
    ) {
        var messageInformation: MessageInformation? = null
        val listErrors: MutableList<AccountRequestError> = mutableListOf()
        if (endpoints.isEmpty()) {
            listErrors.add(
                AccountRequestError(
                    600,
                    "No available endpoints to perform the request"
                )
            )
        }

        refreshTokensIfNeeded(endpoints)
        for (endpoint in endpoints) {
            val apiToken = persistence.apiTokenResponse()?.apiToken
            if (apiToken == null) {
                listErrors.add(AccountRequestError(600, "Invalid request token"))
                break
            }

            if (endpoint.usePinnedCertificate && certificate.isNullOrEmpty()) {
                listErrors.add(
                    AccountRequestError(
                        600,
                        "No available certificate for pinning purposes"
                    )
                )
                continue
            }

            val httpClientConfigResult = if (endpoint.usePinnedCertificate) {
                AccountHttpClient.client(certificate, Pair(endpoint.ipOrRootDomain, endpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }

            val httpClient = httpClientConfigResult.first
            val httpClientError = httpClientConfigResult.second
            if (httpClientError != null) {
                listErrors.add(AccountRequestError(600, httpClientError.message))
                continue
            }

            if (httpClient == null) {
                listErrors.add(AccountRequestError(600, "Invalid http client"))
                continue
            }

            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, Path.MESSAGES)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${Path.MESSAGES.url}"))
                continue
            }

            var succeeded = false
            val platform = when (platform) {
                Platform.IOS -> "ios"
                Platform.ANDROID -> "android"
            }
            val response = httpClient.getCatching<Pair<HttpResponse?, Exception?>> {
                url(url)
                header("Authorization", "Token $apiToken")
                parameter("client", platform)
                parameter("version", appVersion)
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(AccountRequestError(it.status.value, it.status.description))
                } else {
                    try {
                        messageInformation = json.decodeFromString(MessageInformation.serializer(), it.bodyAsText())
                        succeeded = true
                    } catch (exception: SerializationException) {
                        listErrors.add(AccountRequestError(600, "Decode error $exception"))
                    }
                }
            }
            response.second?.let {
                listErrors.add(AccountRequestError(600, it.message))
            }

            // Close the used client explicitly.
            // We need to recreate it due to the possibility of pinning among the endpoints list.
            httpClient.close()

            // If there were no errors in the request for the current endpoint. No need to try the next endpoint.
            if (succeeded) {
                listErrors.clear()
                break
            }
        }

        withContext(Dispatchers.Main) {
            callback(messageInformation, listErrors)
        }
    }

    private suspend fun featureFlagsAsync(
        endpoints: List<AccountEndpoint>,
        callback: (details: FeatureFlagsInformation?, error: List<AccountRequestError>) -> Unit
    ) {
        var flagsInformation: FeatureFlagsInformation? = null
        val listErrors: MutableList<AccountRequestError> = mutableListOf()
        if (endpoints.isEmpty()) {
            listErrors.add(
                AccountRequestError(
                    600,
                    "No available endpoints to perform the request"
                )
            )
        }

        refreshTokensIfNeeded(endpoints)
        for (endpoint in endpoints) {
            if (endpoint.usePinnedCertificate && certificate.isNullOrEmpty()) {
                listErrors.add(
                    AccountRequestError(
                        600,
                        "No available certificate for pinning purposes"
                    )
                )
                continue
            }

            val httpClientConfigResult = if (endpoint.usePinnedCertificate) {
                AccountHttpClient.client(certificate, Pair(endpoint.ipOrRootDomain, endpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }

            val httpClient = httpClientConfigResult.first
            val httpClientError = httpClientConfigResult.second
            if (httpClientError != null) {
                listErrors.add(AccountRequestError(600, httpClientError.message))
                continue
            }

            if (httpClient == null) {
                listErrors.add(AccountRequestError(600, "Invalid http client"))
                continue
            }

            val path = when (platform) {
                Platform.IOS -> Path.IOS_FEATURE_FLAG
                Platform.ANDROID -> Path.ANDROID_FEATURE_FLAG
            }
            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, path)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${path.url}"))
                continue
            }

            var succeeded = false
            val response = httpClient.getCatching<Pair<HttpResponse?, Exception?>> {
                url(url)
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(AccountRequestError(it.status.value, it.status.description))
                } else {
                    try {
                        flagsInformation = json.decodeFromString(FeatureFlagsInformation.serializer(), it.bodyAsText())
                        succeeded = true
                    } catch (exception: SerializationException) {
                        listErrors.add(AccountRequestError(600, "Decode error $exception"))
                    }
                }
            }
            response.second?.let {
                listErrors.add(AccountRequestError(600, it.message))
            }

            // Close the used client explicitly.
            // We need to recreate it due to the possibility of pinning among the endpoints list.
            httpClient.close()

            // If there were no errors in the request for the current endpoint. No need to try the next endpoint.
            if (succeeded) {
                listErrors.clear()
                break
            }
        }

        withContext(Dispatchers.Main) {
            callback(flagsInformation, listErrors)
        }
    }

    // region tokens
    @OptIn(ExperimentalTime::class)
    internal suspend fun refreshTokensIfNeeded(endpoints: List<AccountEndpoint>) {
        val currentInstant =
            Clock.System.now().toLocalDateTime(TimeZone.UTC).toInstant(TimeZone.UTC)

        persistence.apiTokenResponse()?.let { apiTokenResponse ->
            if (currentInstant.daysUntil(
                    apiTokenResponse.expiresAt.toInstant(),
                    TimeZone.UTC
                ) < MIN_EXPIRATION_THRESHOLD_DAYS
            ) {
                refreshApiToken(apiTokenResponse.apiToken, endpoints)
            }

            persistence.vpnTokenResponse()?.let { vpnTokenResponse ->
                if (currentInstant.daysUntil(
                        vpnTokenResponse.expiresAt.toInstant(),
                        TimeZone.UTC
                    ) < MIN_EXPIRATION_THRESHOLD_DAYS
                ) {
                    refreshVpnToken(apiTokenResponse.apiToken, endpoints)
                }
            } ?: refreshVpnToken(apiTokenResponse.apiToken, endpoints)
        }
    }

    private suspend fun refreshApiToken(
        apiToken: String,
        endpoints: List<AccountEndpoint>
    ): List<AccountRequestError> {
        val listErrors: MutableList<AccountRequestError> = mutableListOf()
        if (requestsPipeline.contains(RequestPipeline.API_TOKEN)) {
            listErrors.add(
                AccountRequestError(
                    600,
                    "There is a refresh api token request already in progress"
                )
            )
            return listErrors
        }

        if (endpoints.isEmpty()) {
            listErrors.add(
                AccountRequestError(
                    600,
                    "No available endpoints to perform the request"
                )
            )
            return listErrors
        }

        requestsPipeline.add(RequestPipeline.API_TOKEN)
        for (endpoint in endpoints) {
            if (endpoint.usePinnedCertificate && certificate.isNullOrEmpty()) {
                listErrors.add(
                    AccountRequestError(
                        600,
                        "No available certificate for pinning purposes"
                    )
                )
                continue
            }

            val httpClientConfigResult = if (endpoint.usePinnedCertificate) {
                AccountHttpClient.client(certificate, Pair(endpoint.ipOrRootDomain, endpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }

            val httpClient = httpClientConfigResult.first
            val httpClientError = httpClientConfigResult.second
            if (httpClientError != null) {
                listErrors.add(AccountRequestError(600, httpClientError.message))
                continue
            }

            if (httpClient == null) {
                listErrors.add(AccountRequestError(600, "Invalid http client"))
                continue
            }

            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, Path.REFRESH_API_TOKEN)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${Path.REFRESH_API_TOKEN.url}"))
                continue
            }

            var succeeded = false
            val requestResponse = httpClient.postCatching<Pair<HttpResponse?, Exception?>> {
                url(url)
                header("Authorization", "Token $apiToken")
            }

            requestResponse.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(AccountRequestError(it.status.value, it.status.description))
                } else {
                    try {
                        val apiTokenResponse = json.decodeFromString(ApiTokenResponse.serializer(), it.bodyAsText())
                        persistence.persistApiTokenResponse(apiTokenResponse)
                        succeeded = true
                    } catch (exception: SerializationException) {
                        listErrors.add(AccountRequestError(600, "Decode error $exception"))
                    }
                }
            }
            requestResponse.second?.let {
                listErrors.add(AccountRequestError(600, it.message))
            }

            // Close the used client explicitly.
            // We need to recreate it due to the possibility of pinning among the endpoints list.
            httpClient.close()

            // If there were no errors in the request for the current endpoint. No need to try the next endpoint.
            if (succeeded) {
                listErrors.clear()
                break
            }
        }

        requestsPipeline.remove(RequestPipeline.API_TOKEN)
        return listErrors
    }

    internal suspend fun refreshVpnToken(
        apiToken: String,
        endpoints: List<AccountEndpoint>
    ): List<AccountRequestError> {
        val listErrors: MutableList<AccountRequestError> = mutableListOf()
        if (requestsPipeline.contains(RequestPipeline.VPN_TOKEN)) {
            listErrors.add(
                AccountRequestError(
                    600,
                    "There is a refresh vpn token request already in progress"
                )
            )
            return listErrors
        }

        if (endpoints.isEmpty()) {
            listErrors.add(
                AccountRequestError(
                    600,
                    "No available endpoints to perform the request"
                )
            )
            return listErrors
        }

        requestsPipeline.add(RequestPipeline.VPN_TOKEN)
        for (endpoint in endpoints) {
            if (endpoint.usePinnedCertificate && certificate.isNullOrEmpty()) {
                listErrors.add(
                    AccountRequestError(
                        600,
                        "No available certificate for pinning purposes"
                    )
                )
                continue
            }

            val httpClientConfigResult = if (endpoint.usePinnedCertificate) {
                AccountHttpClient.client(certificate, Pair(endpoint.ipOrRootDomain, endpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }

            val httpClient = httpClientConfigResult.first
            val httpClientError = httpClientConfigResult.second
            if (httpClientError != null) {
                listErrors.add(AccountRequestError(600, httpClientError.message))
                continue
            }

            if (httpClient == null) {
                listErrors.add(AccountRequestError(600, "Invalid http client"))
                continue
            }

            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, Path.VPN_TOKEN)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${Path.VPN_TOKEN.url}"))
                continue
            }

            var succeeded = false
            val requestResponse = httpClient.postCatching<Pair<HttpResponse?, Exception?>> {
                url(url)
                header("Authorization", "Token $apiToken")
            }

            requestResponse.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(AccountRequestError(it.status.value, it.status.description))
                } else {
                    try {
                        persistence.persistVpnTokenResponse(json.decodeFromString(VpnTokenResponse.serializer(), it.bodyAsText()))
                        succeeded = true
                    } catch (exception: SerializationException) {
                        listErrors.add(AccountRequestError(600, "Decode error $exception"))
                    }
                }
            }
            requestResponse.second?.let {
                listErrors.add(AccountRequestError(600, it.message))
            }

            // Close the used client explicitly.
            // We need to recreate it due to the possibility of pinning among the endpoints list.
            httpClient.close()

            // If there were no errors in the request for the current endpoint. No need to try the next endpoint.
            if (succeeded) {
                listErrors.clear()
                break
            }
        }

        requestsPipeline.remove(RequestPipeline.VPN_TOKEN)
        return listErrors
    }
    // endregion

    // endregion

    // region HttpClient extensions
    internal suspend inline fun <reified T> HttpClient.getCatching(
        block: HttpRequestBuilder.() -> Unit = {}
    ): Pair<HttpResponse?, Exception?> {
        var exception: Exception? = null
        var response: HttpResponse? = null
        try {
            response = request {
                method = HttpMethod.Get
                userAgent(userAgentValue)
                apply(block)
            }
        } catch (e: Exception) {
            exception = e
        }
        return Pair(response, exception)
    }

    internal suspend inline fun <reified T> HttpClient.postCatching(
        formParameters: Parameters = Parameters.Empty,
        block: HttpRequestBuilder.() -> Unit = {}
    ): Pair<HttpResponse?, Exception?> {
        var exception: Exception? = null
        var response: HttpResponse? = null
        try {
            response = submitForm(formParameters = formParameters) {
                method = HttpMethod.Post
                userAgent(userAgentValue)
                apply(block)
            }
        } catch (e: Exception) {
            exception = e
        }
        return Pair(response, exception)
    }

    internal suspend inline fun <reified T> HttpClient.deleteCatching(
        formParameters: Parameters = Parameters.Empty,
        block: HttpRequestBuilder.() -> Unit = {}
    ): Pair<HttpResponse?, Exception?> {
        var exception: Exception? = null
        var response: HttpResponse? = null
        try {
            response = submitForm(formParameters = formParameters) {
                method = HttpMethod.Delete
                userAgent(userAgentValue)
                apply(block)
            }
        } catch (e: Exception) {
            exception = e
        }
        return Pair(response, exception)
    }
    // endregion
}