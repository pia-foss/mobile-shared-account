package com.privateinternetaccess.account.internals

import com.privateinternetaccess.account.AccountClientStateProvider
import com.privateinternetaccess.account.AccountEndpoint
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.IOSAccountAPI
import com.privateinternetaccess.account.internals.model.request.IOSLoginReceiptRequest
import com.privateinternetaccess.account.internals.model.response.LoginResponse
import com.privateinternetaccess.account.internals.model.response.SetEmailResponse
import com.privateinternetaccess.account.internals.utils.AccountUtils
import com.privateinternetaccess.account.model.request.IOSPaymentInformation
import com.privateinternetaccess.account.model.request.IOSSignupInformation
import com.privateinternetaccess.account.model.response.IOSSubscriptionInformation
import com.privateinternetaccess.account.model.response.SignUpInformation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*

internal class IOSAccount(
    clientStateProvider: AccountClientStateProvider,
    userAgentValue: String
) : IOSAccountAPI, Account(clientStateProvider, userAgentValue) {

    private enum class Endpoint(val url: String) {
        PAYMENT("/api/client/payment"),
        SUBSCRIPTIONS("/api/client/ios")
    }

    @InternalAPI
    override fun loginWithReceipt(
        receiptBase64: String,
        callback: (token: String?, error: AccountRequestError?) -> Unit
    ) {
        launch {
            loginWithReceiptAsync(receiptBase64, clientStateProvider.accountEndpoints(), callback)
        }
    }

    @InternalAPI
    override fun setEmail(
        username: String,
        password: String,
        email: String,
        resetPassword: Boolean,
        callback: (temporaryPassword: String?, error: AccountRequestError?) -> Unit
    ) {
        launch {
            setEmailAsync(username, password, email, resetPassword, clientStateProvider.accountEndpoints(), callback)
        }
    }

    @InternalAPI
    override fun payment(
        username: String,
        password: String,
        information: IOSPaymentInformation,
        callback: (error: AccountRequestError?) -> Unit
    ) {
        launch {
            paymentAsync(username, password, information, clientStateProvider.accountEndpoints(), callback)
        }
    }

    override fun signUp(
        information: IOSSignupInformation,
        callback: (details: SignUpInformation?, error: AccountRequestError?) -> Unit
    ) {
        launch {
            signUpAsync(information, clientStateProvider.accountEndpoints(), callback)
        }
    }

    override fun subscriptions(
        receipt: String?,
        callback: (details: IOSSubscriptionInformation?, error: AccountRequestError?) -> Unit
    ) {
        launch {
            subscriptionsAsync(receipt, clientStateProvider.accountEndpoints(), callback)
        }
    }
    // endregion

    // region private
    @InternalAPI
    private fun loginWithReceiptAsync(
        receiptBase64: String,
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
                contentType(ContentType.Application.Json)
                body = json.encodeToString(IOSLoginReceiptRequest.serializer(), IOSLoginReceiptRequest(receiptBase64))
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

    @InternalAPI
    private fun setEmailAsync(
        username: String,
        password: String,
        email: String,
        resetPassword: Boolean,
        endpoints: List<AccountEndpoint>,
        callback: (temporaryPassword: String?, error: AccountRequestError?) -> Unit
    ) = async {
        for (accountEndpoint in endpoints) {
            var temporaryPassword: String? = null
            var error: AccountRequestError? = null
            val auth = "$username:$password".encodeBase64()
            val client = if (accountEndpoint.usePinnedCertificate) {
                AccountHttpClient.client(Pair(accountEndpoint.endpoint, accountEndpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }
            val response = client.postCatching<Pair<HttpResponse?, Exception?>> {
                url("https://${accountEndpoint.endpoint}${CommonEndpoint.SET_EMAIL.url}")
                header("Authorization", "Basic $auth")
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

    @InternalAPI
    private fun paymentAsync(
        username: String,
        password: String,
        information: IOSPaymentInformation,
        endpoints: List<AccountEndpoint>,
        callback: (error: AccountRequestError?) -> Unit
    ) = async {
        for (accountEndpoint in endpoints) {
            var error: AccountRequestError? = null
            val auth = "$username:$password".encodeBase64()
            val client = if (accountEndpoint.usePinnedCertificate) {
                AccountHttpClient.client(Pair(accountEndpoint.endpoint, accountEndpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }
            val response = client.postCatching<Pair<HttpResponse?, Exception?>> {
                url("https://${accountEndpoint.endpoint}${Endpoint.PAYMENT.url}")
                contentType(ContentType.Application.Json)
                header("Authorization", "Basic $auth")
                body = json.encodeToString(IOSPaymentInformation.serializer(), information)
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

    private fun signUpAsync(
        information: IOSSignupInformation,
        endpoints: List<AccountEndpoint>,
        callback: (details: SignUpInformation?, error: AccountRequestError?) -> Unit
    ) = async {
        for (accountEndpoint in endpoints) {
            var signUpInformation: SignUpInformation? = null
            var error: AccountRequestError? = null
            val client = if (accountEndpoint.usePinnedCertificate) {
                AccountHttpClient.client(Pair(accountEndpoint.endpoint, accountEndpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }
            val response = client.postCatching<Pair<HttpResponse?, Exception?>> {
                url("https://${accountEndpoint.endpoint}${CommonEndpoint.SIGNUP.url}")
                contentType(ContentType.Application.Json)
                body = json.encodeToString(IOSSignupInformation.serializer(), information)
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    error = AccountRequestError(it.status.value, it.status.description)
                } else {
                    it.content.readUTF8Line()?.let { content ->
                        signUpInformation = json.decodeFromString(SignUpInformation.serializer(), content)
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
                callback(signUpInformation, error)
            }
            break
        }
    }

    private fun subscriptionsAsync(
        receipt: String?,
        endpoints: List<AccountEndpoint>,
        callback: (details: IOSSubscriptionInformation?, error: AccountRequestError?) -> Unit
    ) = async {
        for (accountEndpoint in endpoints) {
            var subscriptionsInformation: IOSSubscriptionInformation? = null
            var error: AccountRequestError? = null
            val client = if (accountEndpoint.usePinnedCertificate) {
                AccountHttpClient.client(Pair(accountEndpoint.endpoint, accountEndpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }
            val response = client.getCatching<Pair<HttpResponse?, Exception?>> {
                url("https://${accountEndpoint.endpoint}${Endpoint.SUBSCRIPTIONS.url}")
                parameter("type", "subscription")
                if (receipt != null) {
                    parameter("receipt", receipt)
                }
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    error = AccountRequestError(it.status.value, it.status.description)
                } else {
                    it.content.readUTF8Line()?.let { content ->
                        subscriptionsInformation =
                            json.decodeFromString(IOSSubscriptionInformation.serializer(), content)
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
                callback(subscriptionsInformation, error)
            }
            break
        }
    }
    // endregion
}