package com.privateinternetaccess.account.internals

import com.privateinternetaccess.account.AccountAPI
import com.privateinternetaccess.account.AccountClientStateProvider
import com.privateinternetaccess.account.AccountEndpoint
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.internals.model.response.LoginResponse
import com.privateinternetaccess.account.internals.model.response.SetEmailResponse
import com.privateinternetaccess.account.internals.utils.AccountUtils
import com.privateinternetaccess.account.model.response.AccountInformation
import com.privateinternetaccess.account.model.response.ClientStatusInformation
import com.privateinternetaccess.account.model.response.InvitesDetailsInformation
import com.privateinternetaccess.account.model.response.RedeemInformation
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext

expect object AccountHttpClient {
    fun client(pinnedEndpoint: Pair<String, String>? = null): HttpClient
}

internal open class Account(
    internal val clientStateProvider: AccountClientStateProvider,
    private val userAgentValue: String
) : CoroutineScope, AccountAPI {

    companion object {
        internal const val REQUEST_TIMEOUT_MS = 3000L
        internal const val certificate = "-----BEGIN CERTIFICATE-----\n" +
                "MIIHqzCCBZOgAwIBAgIJAJ0u+vODZJntMA0GCSqGSIb3DQEBDQUAMIHoMQswCQYD\n" +
                "VQQGEwJVUzELMAkGA1UECBMCQ0ExEzARBgNVBAcTCkxvc0FuZ2VsZXMxIDAeBgNV\n" +
                "BAoTF1ByaXZhdGUgSW50ZXJuZXQgQWNjZXNzMSAwHgYDVQQLExdQcml2YXRlIElu\n" +
                "dGVybmV0IEFjY2VzczEgMB4GA1UEAxMXUHJpdmF0ZSBJbnRlcm5ldCBBY2Nlc3Mx\n" +
                "IDAeBgNVBCkTF1ByaXZhdGUgSW50ZXJuZXQgQWNjZXNzMS8wLQYJKoZIhvcNAQkB\n" +
                "FiBzZWN1cmVAcHJpdmF0ZWludGVybmV0YWNjZXNzLmNvbTAeFw0xNDA0MTcxNzQw\n" +
                "MzNaFw0zNDA0MTIxNzQwMzNaMIHoMQswCQYDVQQGEwJVUzELMAkGA1UECBMCQ0Ex\n" +
                "EzARBgNVBAcTCkxvc0FuZ2VsZXMxIDAeBgNVBAoTF1ByaXZhdGUgSW50ZXJuZXQg\n" +
                "QWNjZXNzMSAwHgYDVQQLExdQcml2YXRlIEludGVybmV0IEFjY2VzczEgMB4GA1UE\n" +
                "AxMXUHJpdmF0ZSBJbnRlcm5ldCBBY2Nlc3MxIDAeBgNVBCkTF1ByaXZhdGUgSW50\n" +
                "ZXJuZXQgQWNjZXNzMS8wLQYJKoZIhvcNAQkBFiBzZWN1cmVAcHJpdmF0ZWludGVy\n" +
                "bmV0YWNjZXNzLmNvbTCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBALVk\n" +
                "hjumaqBbL8aSgj6xbX1QPTfTd1qHsAZd2B97m8Vw31c/2yQgZNf5qZY0+jOIHULN\n" +
                "De4R9TIvyBEbvnAg/OkPw8n/+ScgYOeH876VUXzjLDBnDb8DLr/+w9oVsuDeFJ9K\n" +
                "V2UFM1OYX0SnkHnrYAN2QLF98ESK4NCSU01h5zkcgmQ+qKSfA9Ny0/UpsKPBFqsQ\n" +
                "25NvjDWFhCpeqCHKUJ4Be27CDbSl7lAkBuHMPHJs8f8xPgAbHRXZOxVCpayZ2SND\n" +
                "fCwsnGWpWFoMGvdMbygngCn6jA/W1VSFOlRlfLuuGe7QFfDwA0jaLCxuWt/BgZyl\n" +
                "p7tAzYKR8lnWmtUCPm4+BtjyVDYtDCiGBD9Z4P13RFWvJHw5aapx/5W/CuvVyI7p\n" +
                "Kwvc2IT+KPxCUhH1XI8ca5RN3C9NoPJJf6qpg4g0rJH3aaWkoMRrYvQ+5PXXYUzj\n" +
                "tRHImghRGd/ydERYoAZXuGSbPkm9Y/p2X8unLcW+F0xpJD98+ZI+tzSsI99Zs5wi\n" +
                "jSUGYr9/j18KHFTMQ8n+1jauc5bCCegN27dPeKXNSZ5riXFL2XX6BkY68y58UaNz\n" +
                "meGMiUL9BOV1iV+PMb7B7PYs7oFLjAhh0EdyvfHkrh/ZV9BEhtFa7yXp8XR0J6vz\n" +
                "1YV9R6DYJmLjOEbhU8N0gc3tZm4Qz39lIIG6w3FDAgMBAAGjggFUMIIBUDAdBgNV\n" +
                "HQ4EFgQUrsRtyWJftjpdRM0+925Y6Cl08SUwggEfBgNVHSMEggEWMIIBEoAUrsRt\n" +
                "yWJftjpdRM0+925Y6Cl08SWhge6kgeswgegxCzAJBgNVBAYTAlVTMQswCQYDVQQI\n" +
                "EwJDQTETMBEGA1UEBxMKTG9zQW5nZWxlczEgMB4GA1UEChMXUHJpdmF0ZSBJbnRl\n" +
                "cm5ldCBBY2Nlc3MxIDAeBgNVBAsTF1ByaXZhdGUgSW50ZXJuZXQgQWNjZXNzMSAw\n" +
                "HgYDVQQDExdQcml2YXRlIEludGVybmV0IEFjY2VzczEgMB4GA1UEKRMXUHJpdmF0\n" +
                "ZSBJbnRlcm5ldCBBY2Nlc3MxLzAtBgkqhkiG9w0BCQEWIHNlY3VyZUBwcml2YXRl\n" +
                "aW50ZXJuZXRhY2Nlc3MuY29tggkAnS7684Nkme0wDAYDVR0TBAUwAwEB/zANBgkq\n" +
                "hkiG9w0BAQ0FAAOCAgEAJsfhsPk3r8kLXLxY+v+vHzbr4ufNtqnL9/1Uuf8NrsCt\n" +
                "pXAoyZ0YqfbkWx3NHTZ7OE9ZRhdMP/RqHQE1p4N4Sa1nZKhTKasV6KhHDqSCt/dv\n" +
                "Em89xWm2MVA7nyzQxVlHa9AkcBaemcXEiyT19XdpiXOP4Vhs+J1R5m8zQOxZlV1G\n" +
                "tF9vsXmJqWZpOVPmZ8f35BCsYPvv4yMewnrtAC8PFEK/bOPeYcKN50bol22QYaZu\n" +
                "LfpkHfNiFTnfMh8sl/ablPyNY7DUNiP5DRcMdIwmfGQxR5WEQoHL3yPJ42LkB5zs\n" +
                "6jIm26DGNXfwura/mi105+ENH1CaROtRYwkiHb08U6qLXXJz80mWJkT90nr8Asj3\n" +
                "5xN2cUppg74nG3YVav/38P48T56hG1NHbYF5uOCske19F6wi9maUoto/3vEr0rnX\n" +
                "JUp2KODmKdvBI7co245lHBABWikk8VfejQSlCtDBXn644ZMtAdoxKNfR2WTFVEwJ\n" +
                "iyd1Fzx0yujuiXDROLhISLQDRjVVAvawrAtLZWYK31bY7KlezPlQnl/D9Asxe85l\n" +
                "8jO5+0LdJ6VyOs/Hd4w52alDW/MFySDZSfQHMTIc30hLBJ8OnCEIvluVQQ2UQvoW\n" +
                "+no177N9L2Y+M9TcTA62ZyMXShHQGeh20rb4kK8f+iFX8NxtdHVSkxMEFSfDDyQ=\n" +
                "-----END CERTIFICATE-----\n"
    }

    internal val json = Json { ignoreUnknownKeys = true; encodeDefaults = false }

    internal enum class CommonMetaEndpoint(val url: String) {
        LOGIN("/apiv2/token"),
    }

    private enum class MetaEndpoint(val url: String) {
        LOGIN_LINK("/apiv2/login_link"),
        LOGOUT("/apiv2/expire_token"),
        ACCOUNT_DETAILS("/apiv2/account"),
    }

    internal enum class CommonEndpoint(val url: String) {
        LOGIN("/api/client/v2/token"),
        SIGNUP("/api/client/signup"),
        SET_EMAIL("/api/client/account"),
    }

    private enum class Endpoint(val url: String) {
        LOGIN_LINK("/api/client/v2/login_link"),
        LOGOUT("/api/client/v2/expire_token"),
        ACCOUNT_DETAILS("/api/client/v2/account"),
        CLIENT_STATUS("/api/client/status"),
        INVITES("/api/client/invites"),
        REDEEM("/api/client/giftcard_redeem")
    }

    // region CoroutineScope
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
    // endregion

    // region AccountAPI
    override fun loginLink(email: String, callback: (error: AccountRequestError?) -> Unit) {
        launch {
            loginLinkAsync(email, clientStateProvider.accountEndpoints(), callback)
        }
    }

    override fun loginWithCredentials(
        username: String,
        password: String,
        callback: (token: String?, error: AccountRequestError?) -> Unit
    ) {
        launch {
            loginWithCredentialsAsync(username, password, clientStateProvider.accountEndpoints(), callback)
        }
    }

    override fun logout(token: String, callback: (error: AccountRequestError?) -> Unit) {
        launch {
            logoutAsync(token, clientStateProvider.accountEndpoints(), callback)
        }
    }

    override fun accountDetails(
        token: String,
        callback: (details: AccountInformation?, error: AccountRequestError?) -> Unit
    ) {
        launch {
            accountDetailsAsync(token, clientStateProvider.accountEndpoints(), callback)
        }
    }

    override fun clientStatus(
        callback: (status: ClientStatusInformation?, error: AccountRequestError?) -> Unit
    ) {
        launch {
            clientStatusAsync(clientStateProvider.accountEndpoints(), callback)
        }
    }

    override fun setEmail(
        token: String,
        email: String,
        resetPassword: Boolean,
        callback: (temporaryPassword: String?, error: AccountRequestError?) -> Unit
    ) {
        launch {
            setEmailAsync(token, email, resetPassword, clientStateProvider.accountEndpoints(), callback)
        }
    }

    override fun sendInvite(
        token: String,
        recipientEmail: String,
        recipientName: String,
        callback: (error: AccountRequestError?) -> Unit
    ) {
        launch {
            sendInviteAsync(token, recipientEmail, recipientName, clientStateProvider.accountEndpoints(), callback)
        }
    }

    override fun invitesDetails(
        token: String,
        callback: (details: InvitesDetailsInformation?, error: AccountRequestError?) -> Unit
    ) {
        launch {
            invitesDetailsAsync(token, clientStateProvider.accountEndpoints(), callback)
        }
    }

    override fun redeem(
        email: String,
        code: String,
        callback: (details: RedeemInformation?, error: AccountRequestError?) -> Unit
    ) {
        launch {
            redeemAsync(email, code, clientStateProvider.accountEndpoints(), callback)
        }
    }
    // endregion

    // region private
    private fun loginLinkAsync(
        email: String,
        endpoints: List<AccountEndpoint>,
        callback: (error: AccountRequestError?) -> Unit
    ) = async {
        for (accountEndpoint in endpoints) {
            var error: AccountRequestError? = null
            var subdomain: String?
            val client = if (accountEndpoint.usePinnedCertificate) {
                subdomain = MetaEndpoint.LOGIN_LINK.url
                AccountHttpClient.client(Pair(accountEndpoint.endpoint, accountEndpoint.certificateCommonName!!))
            } else {
                subdomain = Endpoint.LOGIN_LINK.url
                AccountHttpClient.client()
            }
            val response = client.postCatching<Pair<HttpResponse?, Exception?>> {
                url("https://${accountEndpoint.endpoint}$subdomain")
                parameter("email", email)
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    error = AccountRequestError(it.status.value, it.status.description)
                }
            }
            response.second?.let {
                error = AccountRequestError(600, it.message)
            }

            // If there has been an error and it's not the last endpoint. Continue to the next one.
            if (error != null && accountEndpoint != endpoints.last()) {
                continue
            }

            // If the request was successful or we exhausted the list of endpoints.
            // Report the request result and break the loop.
            withContext(Dispatchers.Main) {
                callback(error)
            }
            break
        }
    }

    private fun loginWithCredentialsAsync(
        username: String,
        password: String,
        endpoints: List<AccountEndpoint>,
        callback: (token: String?, error: AccountRequestError?) -> Unit
    ) = async {
        for (accountEndpoint in endpoints) {
            var token: String? = null
            var error: AccountRequestError? = null
            var subdomain: String?
            val client = if (accountEndpoint.usePinnedCertificate) {
                subdomain = CommonMetaEndpoint.LOGIN.url
                AccountHttpClient.client(Pair(accountEndpoint.endpoint, accountEndpoint.certificateCommonName!!))
            } else {
                subdomain = CommonEndpoint.LOGIN.url
                AccountHttpClient.client()
            }
            val response = client.postCatching<Pair<HttpResponse?, Exception?>> {
                url("https://${accountEndpoint.endpoint}$subdomain")
                parameter("username", username)
                parameter("password", password)
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    error = AccountRequestError(it.status.value, it.status.description)
                } else {
                    it.content.readUTF8Line()?.let { content ->
                        token = json.decodeFromString(LoginResponse.serializer(), content).token
                    } ?: run {
                        error = AccountRequestError(600, "Request response undefined")
                    }
                }
            }
            response.second?.let {
                error = AccountRequestError(600, it.message)
            }

            // If there has been an error and it's not the last endpoint. Continue to the next one.
            if (error != null && accountEndpoint != endpoints.last()) {
                continue
            }

            // If the request was successful or we exhausted the list of endpoints.
            // Report the request result and break the loop.
            withContext(Dispatchers.Main) {
                callback(token, error)
            }
            break
        }
    }

    private fun logoutAsync(
        token: String,
        endpoints: List<AccountEndpoint>,
        callback: (error: AccountRequestError?) -> Unit
    ) = async {
        for (accountEndpoint in endpoints) {
            var error: AccountRequestError? = null
            var subdomain: String?
            val client = if (accountEndpoint.usePinnedCertificate) {
                subdomain = MetaEndpoint.LOGOUT.url
                AccountHttpClient.client(Pair(accountEndpoint.endpoint, accountEndpoint.certificateCommonName!!))
            } else {
                subdomain = Endpoint.LOGOUT.url
                AccountHttpClient.client()
            }
            val response = client.postCatching<Pair<HttpResponse?, Exception?>> {
                url("https://${accountEndpoint.endpoint}$subdomain")
                header("Authorization", "Token $token")
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    error = AccountRequestError(it.status.value, it.status.description)
                }
            }
            response.second?.let {
                error = AccountRequestError(600, it.message)
            }

            // If there has been an error and it's not the last endpoint. Continue to the next one.
            if (error != null && accountEndpoint != endpoints.last()) {
                continue
            }

            // If the request was successful or we exhausted the list of endpoints.
            // Report the request result and break the loop.
            withContext(Dispatchers.Main) {
                callback(error)
            }
            break
        }
    }

    private fun accountDetailsAsync(
        token: String,
        endpoints: List<AccountEndpoint>,
        callback: (details: AccountInformation?, error: AccountRequestError?) -> Unit
    ) = async {
        for (accountEndpoint in endpoints) {
            var accountInformation: AccountInformation? = null
            var error: AccountRequestError? = null
            var subdomain: String?
            val client = if (accountEndpoint.usePinnedCertificate) {
                subdomain = MetaEndpoint.ACCOUNT_DETAILS.url
                AccountHttpClient.client(Pair(accountEndpoint.endpoint, accountEndpoint.certificateCommonName!!))
            } else {
                subdomain = Endpoint.ACCOUNT_DETAILS.url
                AccountHttpClient.client()
            }
            val response = client.getCatching<Pair<HttpResponse?, Exception?>> {
                url("https://${accountEndpoint.endpoint}$subdomain")
                header("Authorization", "Token $token")
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    error = AccountRequestError(it.status.value, it.status.description)
                } else {
                    it.content.readUTF8Line()?.let { content ->
                        accountInformation = json.decodeFromString(AccountInformation.serializer(), content)
                    } ?: run {
                        error = AccountRequestError(600, "Request response undefined")
                    }
                }
            }
            response.second?.let {
                error = AccountRequestError(600, it.message)
            }

            // If there has been an error and it's not the last endpoint. Continue to the next one.
            if (error != null && accountEndpoint != endpoints.last()) {
                continue
            }

            // If the request was successful or we exhausted the list of endpoints.
            // Report the request result and break the loop.
            withContext(Dispatchers.Main) {
                callback(accountInformation, error)
            }
            break
        }
    }

    private fun clientStatusAsync(
        endpoints: List<AccountEndpoint>,
        callback: (status: ClientStatusInformation?, error: AccountRequestError?) -> Unit
    ) = async {
        for (accountEndpoint in endpoints) {
            var clientStatus: ClientStatusInformation? = null
            var error: AccountRequestError? = null
            val client = if (accountEndpoint.usePinnedCertificate) {
                AccountHttpClient.client(Pair(accountEndpoint.endpoint, accountEndpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }
            val response = client.getCatching<Pair<HttpResponse?, Exception?>> {
                url("https://${accountEndpoint.endpoint}${Endpoint.CLIENT_STATUS.url}")
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    error = AccountRequestError(it.status.value, it.status.description)
                } else {
                    it.content.readUTF8Line()?.let { content ->
                        clientStatus = json.decodeFromString(ClientStatusInformation.serializer(), content)
                    } ?: run {
                        error = AccountRequestError(600, "Request response undefined")
                    }
                }
            }
            response.second?.let {
                error = AccountRequestError(600, it.message)
            }

            // Avoid using proxies for client status as they'll return the proxy ip rather than the client one.
            // Ignore the response.
            if (accountEndpoint.isProxy) {
                clientStatus = null
                error = AccountRequestError(600, "Ignoring response on proxy endpoint")
            }

            // If there has been an error and it's not the last endpoint. Continue to the next one.
            if (error != null && accountEndpoint != endpoints.last()) {
                continue
            }

            // If the request was successful or we exhausted the list of endpoints.
            // Report the request result and break the loop.
            withContext(Dispatchers.Main) {
                callback(clientStatus, error)
            }
            break
        }
    }

    private fun setEmailAsync(
        token: String,
        email: String,
        resetPassword: Boolean,
        endpoints: List<AccountEndpoint>,
        callback: (temporaryPassword: String?, error: AccountRequestError?) -> Unit
    ) = async {
        for (accountEndpoint in endpoints) {
            var temporaryPassword: String? = null
            var error: AccountRequestError? = null
            val client = if (accountEndpoint.usePinnedCertificate) {
                AccountHttpClient.client(Pair(accountEndpoint.endpoint, accountEndpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }
            val response = client.postCatching<Pair<HttpResponse?, Exception?>> {
                url("https://${accountEndpoint.endpoint}${CommonEndpoint.SET_EMAIL.url}")
                header("Authorization", "Token $token")
                parameter("email", email)
                parameter("reset_password", resetPassword)
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    error = AccountRequestError(it.status.value, it.status.description)
                } else {
                    it.content.readUTF8Line()?.let { content ->
                        temporaryPassword = json.decodeFromString(SetEmailResponse.serializer(), content).password
                    } ?: run {
                        error = AccountRequestError(600, "Request response undefined")
                    }
                }
            }
            response.second?.let {
                error = AccountRequestError(600, it.message)
            }

            // If there has been an error and it's not the last endpoint. Continue to the next one.
            if (error != null && accountEndpoint != endpoints.last()) {
                continue
            }

            // If the request was successful or we exhausted the list of endpoints.
            // Report the request result and break the loop.
            withContext(Dispatchers.Main) {
                callback(temporaryPassword, error)
            }
            break
        }
    }

    private fun sendInviteAsync(
        token: String,
        recipientEmail: String,
        recipientName: String,
        endpoints: List<AccountEndpoint>,
        callback: (error: AccountRequestError?) -> Unit
    ) = async {
        for (accountEndpoint in endpoints) {
            var error: AccountRequestError? = null
            val client = if (accountEndpoint.usePinnedCertificate) {
                AccountHttpClient.client(Pair(accountEndpoint.endpoint, accountEndpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }
            val response = client.postCatching<Pair<HttpResponse?, Exception?>> {
                url("https://${accountEndpoint.endpoint}${Endpoint.INVITES.url}")
                header("Authorization", "Token $token")
                parameter("invitee_email", recipientEmail)
                parameter("invitee_name", recipientName)
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    error = AccountRequestError(it.status.value, it.status.description)
                }
            }
            response.second?.let {
                error = AccountRequestError(600, it.message)
            }

            // If there has been an error and it's not the last endpoint. Continue to the next one.
            if (error != null && accountEndpoint != endpoints.last()) {
                continue
            }

            // If the request was successful or we exhausted the list of endpoints.
            // Report the request result and break the loop.
            withContext(Dispatchers.Main) {
                callback(error)
            }
            break
        }
    }

    private fun invitesDetailsAsync(
        token: String,
        endpoints: List<AccountEndpoint>,
        callback: (details: InvitesDetailsInformation?, error: AccountRequestError?) -> Unit
    ) = async {
        for (accountEndpoint in endpoints) {
            var invitesDetailsInformation: InvitesDetailsInformation? = null
            var error: AccountRequestError? = null
            val client = if (accountEndpoint.usePinnedCertificate) {
                AccountHttpClient.client(Pair(accountEndpoint.endpoint, accountEndpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }
            val response = client.getCatching<Pair<HttpResponse?, Exception?>> {
                url("https://${accountEndpoint.endpoint}${Endpoint.INVITES.url}")
                header("Authorization", "Token $token")
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    error = AccountRequestError(it.status.value, it.status.description)
                } else {
                    it.content.readUTF8Line()?.let { content ->
                        invitesDetailsInformation =
                            json.decodeFromString(InvitesDetailsInformation.serializer(), content)
                    } ?: run {
                        error = AccountRequestError(600, "Request response undefined")
                    }
                }
            }
            response.second?.let {
                error = AccountRequestError(600, it.message)
            }

            // If there has been an error and it's not the last endpoint. Continue to the next one.
            if (error != null && accountEndpoint != endpoints.last()) {
                continue
            }

            // If the request was successful or we exhausted the list of endpoints.
            // Report the request result and break the loop.
            withContext(Dispatchers.Main) {
                callback(invitesDetailsInformation, error)
            }
            break
        }
    }

    private fun redeemAsync(
        email: String,
        code: String,
        endpoints: List<AccountEndpoint>,
        callback: (details: RedeemInformation?, error: AccountRequestError?) -> Unit
    ) = async {
        for (accountEndpoint in endpoints) {
            var redeemInformation: RedeemInformation? = null
            var error: AccountRequestError? = null
            val client = if (accountEndpoint.usePinnedCertificate) {
                AccountHttpClient.client(Pair(accountEndpoint.endpoint, accountEndpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }
            val response = client.postCatching<Pair<HttpResponse?, Exception?>> {
                url("https://${accountEndpoint.endpoint}${Endpoint.REDEEM.url}")
                parameter("email", email)
                parameter("pin", code)
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    error = AccountRequestError(it.status.value, it.status.description)
                } else {
                    it.content.readUTF8Line()?.let { content ->
                        redeemInformation = json.decodeFromString(RedeemInformation.serializer(), content)
                    } ?: run {
                        error = AccountRequestError(600, "Request response undefined")
                    }
                }
            }
            response.second?.let {
                error = AccountRequestError(600, it.message)
            }

            // If there has been an error and it's not the last endpoint. Continue to the next one.
            if (error != null && accountEndpoint != endpoints.last()) {
                continue
            }

            // If the request was successful or we exhausted the list of endpoints.
            // Report the request result and break the loop.
            withContext(Dispatchers.Main) {
                callback(redeemInformation, error)
            }
            break
        }
    }
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
        block: HttpRequestBuilder.() -> Unit = {}
    ): Pair<HttpResponse?, Exception?> = request {
        var exception: Exception? = null
        var response: HttpResponse? = null
        try {
            response = request {
                method = HttpMethod.Post
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