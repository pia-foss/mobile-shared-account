package com.privateinternetaccess.account.internals

import com.privateinternetaccess.account.AccountAPI
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.Platform
import com.privateinternetaccess.account.internals.model.response.LoginResponse
import com.privateinternetaccess.account.internals.model.response.SetEmailResponse
import com.privateinternetaccess.account.internals.utils.AccountUtils
import com.privateinternetaccess.account.model.response.AccountInformation
import com.privateinternetaccess.account.model.response.ClientStatusInformation
import com.privateinternetaccess.account.model.response.InvitesDetailsInformation
import com.privateinternetaccess.account.model.response.RedeemInformation
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlin.coroutines.CoroutineContext

internal open class Account(
        private val stagingEndpoint: String?,
        private val platform: Platform,
        private val userAgentValue: String
) : CoroutineScope, AccountAPI {

    companion object {
        private const val REQUEST_TIMEOUT_MS = 5000L
    }

    internal val json = Json(JsonConfiguration(ignoreUnknownKeys = true, encodeDefaults = false))
    internal var baseUrl = stagingEndpoint ?: "https://www.privateinternetaccess.com"

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

    internal val client = HttpClient {
        expectSuccess = false
        install(HttpTimeout) {
            requestTimeoutMillis = REQUEST_TIMEOUT_MS
        }
    }

    // region CoroutineScope
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
    // endregion

    override fun loginLink(email: String, callback: (error: AccountRequestError?) -> Unit) {
        launch {
            loginLinkAsync(email, callback)
        }
    }

    override fun loginWithCredentials(
            username: String,
            password: String,
            callback: (token: String?, error: AccountRequestError?) -> Unit
    ) {
        launch {
            loginWithCredentialsAsync(username, password, callback)
        }
    }

    override fun logout(token: String, callback: (error: AccountRequestError?) -> Unit) {
        launch {
            logoutAsync(token, callback)
        }
    }

    override fun accountDetails(
            token: String,
            callback: (details: AccountInformation?, error: AccountRequestError?) -> Unit
    ) {
        launch {
            accountDetailsAsync(token, callback)
        }
    }

    override fun clientStatus(
            callback: (status: ClientStatusInformation?, error: AccountRequestError?) -> Unit
    ) {
        launch {
            clientStatusAsync(callback)
        }
    }

    override fun setEmail(
            token: String,
            email: String,
            resetPassword: Boolean,
            callback: (temporaryPassword: String?, error: AccountRequestError?) -> Unit
    ) {
        launch {
            setEmailAsync(token, email, resetPassword, callback)
        }
    }

    override fun sendInvite(
            token: String,
            recipientEmail: String,
            recipientName: String,
            callback: (error: AccountRequestError?) -> Unit
    ) {
        launch {
            sendInviteAsync(token, recipientEmail, recipientName, callback)
        }
    }

    override fun invitesDetails(
            token: String,
            callback: (details: InvitesDetailsInformation?, error: AccountRequestError?) -> Unit
    ) {
        launch {
            invitesDetailsAsync(token, callback)
        }
    }

    override fun redeem(
            email: String,
            code: String,
            callback: (details: RedeemInformation?, error: AccountRequestError?) -> Unit
    ) {
        launch {
            redeemAsync(email, code, callback)
        }
    }

    override fun isStaging(): Boolean {
        return !stagingEndpoint.isNullOrEmpty()
    }
    // endregion

    // region private
    private fun loginLinkAsync(email: String, callback: (error: AccountRequestError?) -> Unit) = async {
        var error: AccountRequestError? = null
        val response = client.postCatching<Pair<HttpResponse?, Exception?>> {
            url(baseUrl + Endpoint.LOGIN_LINK.url)
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

        withContext(Dispatchers.Main) {
            callback(error)
        }
    }

    private fun loginWithCredentialsAsync(
            username: String,
            password: String,
            callback: (token: String?, error: AccountRequestError?) -> Unit
    ) = async {
        var token: String? = null
        var error: AccountRequestError? = null
        val response = client.postCatching<Pair<HttpResponse?, Exception?>> {
            url(baseUrl + CommonEndpoint.LOGIN.url)
            parameter("username", username)
            parameter("password", password)
        }

        response.first?.let {
            if (AccountUtils.isErrorStatusCode(it.status.value)) {
                error = AccountRequestError(it.status.value, it.status.description)
            } else {
                it.content.readUTF8Line()?.let { content ->
                    token = json.parse(LoginResponse.serializer(), content).token
                } ?: run {
                    error = AccountRequestError(600, "Request response undefined")
                }
            }
        }
        response.second?.let {
            error = AccountRequestError(600, it.message)
        }

        withContext(Dispatchers.Main) {
            callback(token, error)
        }
    }

    private fun logoutAsync(
            token: String,
            callback: (error: AccountRequestError?) -> Unit
    ) = async {
        var error: AccountRequestError? = null
        val response = client.postCatching<Pair<HttpResponse?, Exception?>> {
            url(baseUrl + Endpoint.LOGOUT.url)
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

        withContext(Dispatchers.Main) {
            callback(error)
        }
    }

    private fun accountDetailsAsync(
            token: String,
            callback: (details: AccountInformation?, error: AccountRequestError?) -> Unit
    ) = async {
        var accountInformation: AccountInformation? = null
        var error: AccountRequestError? = null
        val response = client.getCatching<Pair<HttpResponse?, Exception?>> {
            url(baseUrl + Endpoint.ACCOUNT_DETAILS.url)
            header("Authorization", "Token $token")
        }

        response.first?.let {
            if (AccountUtils.isErrorStatusCode(it.status.value)) {
                error = AccountRequestError(it.status.value, it.status.description)
            } else {
                it.content.readUTF8Line()?.let { content ->
                    accountInformation = json.parse(AccountInformation.serializer(), content)
                } ?: run {
                    error = AccountRequestError(600, "Request response undefined")
                }
            }
        }
        response.second?.let {
            error = AccountRequestError(600, it.message)
        }

        withContext(Dispatchers.Main) {
            callback(accountInformation, error)
        }
    }

    private fun clientStatusAsync(
            callback: (status: ClientStatusInformation?, error: AccountRequestError?) -> Unit
    ) = async {
        var clientStatus: ClientStatusInformation? = null
        var error: AccountRequestError? = null
        val response = client.getCatching<Pair<HttpResponse?, Exception?>> {
            url(baseUrl + Endpoint.CLIENT_STATUS.url)
        }

        response.first?.let {
            if (AccountUtils.isErrorStatusCode(it.status.value)) {
                error = AccountRequestError(it.status.value, it.status.description)
            } else {
                it.content.readUTF8Line()?.let { content ->
                    clientStatus = json.parse(ClientStatusInformation.serializer(), content)
                } ?: run {
                    error = AccountRequestError(600, "Request response undefined")
                }
            }
        }
        response.second?.let {
            error = AccountRequestError(600, it.message)
        }

        withContext(Dispatchers.Main) {
            callback(clientStatus, error)
        }
    }

    private fun setEmailAsync(
            token: String,
            email: String,
            resetPassword: Boolean,
            callback: (temporaryPassword: String?, error: AccountRequestError?) -> Unit
    ) = async {
        var temporaryPassword: String? = null
        var error: AccountRequestError? = null
        val response = client.postCatching<Pair<HttpResponse?, Exception?>> {
            url(baseUrl + CommonEndpoint.SET_EMAIL.url)
            header("Authorization", "Token $token")
            parameter("email", email)
            parameter("reset_password", resetPassword)
        }

        response.first?.let {
            if (AccountUtils.isErrorStatusCode(it.status.value)) {
                error = AccountRequestError(it.status.value, it.status.description)
            } else {
                it.content.readUTF8Line()?.let { content ->
                    temporaryPassword = json.parse(SetEmailResponse.serializer(), content).password
                } ?: run {
                    error = AccountRequestError(600, "Request response undefined")
                }
            }
        }
        response.second?.let {
            error = AccountRequestError(600, it.message)
        }

        withContext(Dispatchers.Main) {
            callback(temporaryPassword, error)
        }
    }

    private fun sendInviteAsync(
            token: String,
            recipientEmail: String,
            recipientName: String,
            callback: (error: AccountRequestError?) -> Unit
    ) = async {
        var error: AccountRequestError? = null
        val response = client.postCatching<Pair<HttpResponse?, Exception?>> {
            url(baseUrl + Endpoint.INVITES.url)
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

        withContext(Dispatchers.Main) {
            callback(error)
        }
    }

    private fun invitesDetailsAsync(
            token: String,
            callback: (details: InvitesDetailsInformation?, error: AccountRequestError?) -> Unit
    ) = async {
        var invitesDetailsInformation: InvitesDetailsInformation? = null
        var error: AccountRequestError? = null
        val response = client.getCatching<Pair<HttpResponse?, Exception?>> {
            url(baseUrl + Endpoint.INVITES.url)
            header("Authorization", "Token $token")
        }

        response.first?.let {
            if (AccountUtils.isErrorStatusCode(it.status.value)) {
                error = AccountRequestError(it.status.value, it.status.description)
            } else {
                it.content.readUTF8Line()?.let { content ->
                    invitesDetailsInformation = json.parse(InvitesDetailsInformation.serializer(), content)
                } ?: run {
                    error = AccountRequestError(600, "Request response undefined")
                }
            }
        }
        response.second?.let {
            error = AccountRequestError(600, it.message)
        }

        withContext(Dispatchers.Main) {
            callback(invitesDetailsInformation, error)
        }
    }

    private fun redeemAsync(
            email: String,
            code: String,
            callback: (details: RedeemInformation?, error: AccountRequestError?) -> Unit
    ) = async {
        var redeemInformation: RedeemInformation? = null
        var error: AccountRequestError? = null
        val response = client.postCatching<Pair<HttpResponse?, Exception?>> {
            url(baseUrl + Endpoint.REDEEM.url)
            parameter("email", email)
            parameter("pin", code)
        }

        response.first?.let {
            if (AccountUtils.isErrorStatusCode(it.status.value)) {
                error = AccountRequestError(it.status.value, it.status.description)
            } else {
                it.content.readUTF8Line()?.let { content ->
                    redeemInformation = json.parse(RedeemInformation.serializer(), content)
                } ?: run {
                    error = AccountRequestError(600, "Request response undefined")
                }
            }
        }
        response.second?.let {
            error = AccountRequestError(600, it.message)
        }

        withContext(Dispatchers.Main) {
            callback(redeemInformation, error)
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