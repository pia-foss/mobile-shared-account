package com.privateinternetaccess.account.internals

import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.IOSAccountAPI
import com.privateinternetaccess.account.Platform
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
    stagingEndpoint: String?,
    userAgentValue: String
) : IOSAccountAPI, Account(stagingEndpoint, Platform.IOS, userAgentValue) {

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
            loginWithReceiptAsync(receiptBase64, callback)
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
            setEmailAsync(username, password, email, resetPassword, callback)
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
            paymentAsync(username, password, information, callback)
        }
    }

    override fun signUp(
        information: IOSSignupInformation,
        callback: (details: SignUpInformation?, error: AccountRequestError?) -> Unit
    ) {
        launch {
            signUpAsync(information, callback)
        }
    }

    override fun subscriptions(
        receipt: String?,
        callback: (details: IOSSubscriptionInformation?, error: AccountRequestError?) -> Unit
    ) {
        launch {
            subscriptionsAsync(receipt, callback)
        }
    }
    // endregion

    // region private
    @InternalAPI
    private fun loginWithReceiptAsync(
        receiptBase64: String,
        callback: (token: String?, error: AccountRequestError?) -> Unit
    ) = async {
        var token: String? = null
        var error: AccountRequestError? = null
        val response = client.postCatching<Pair<HttpResponse?, Exception?>> {
            url(baseUrl + CommonEndpoint.LOGIN.url)
            contentType(ContentType.Application.Json)
            body = json.stringify(IOSLoginReceiptRequest.serializer(), IOSLoginReceiptRequest(receiptBase64))
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

    @InternalAPI
    private fun setEmailAsync(
        username: String,
        password: String,
        email: String,
        resetPassword: Boolean,
        callback: (temporaryPassword: String?, error: AccountRequestError?) -> Unit
    ) = async {
        var temporaryPassword: String? = null
        var error: AccountRequestError? = null
        val auth = "$username:$password".encodeBase64()
        val response = client.postCatching<Pair<HttpResponse?, Exception?>> {
            url(baseUrl + CommonEndpoint.SET_EMAIL.url)
            header("Authorization", "Basic $auth")
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

    @InternalAPI
    private fun paymentAsync(
        username: String,
        password: String,
        information: IOSPaymentInformation,
        callback: (error: AccountRequestError?) -> Unit
    ) = async {
        var error: AccountRequestError? = null
        val auth = "$username:$password".encodeBase64()
        val response = client.postCatching<Pair<HttpResponse?, Exception?>> {
            url(baseUrl + Endpoint.PAYMENT.url)
            contentType(ContentType.Application.Json)
            header("Authorization", "Basic $auth")
            body = json.stringify(IOSPaymentInformation.serializer(), information)
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

    private fun signUpAsync(
        information: IOSSignupInformation,
        callback: (details: SignUpInformation?, error: AccountRequestError?) -> Unit
    ) = async {
        var signUpInformation: SignUpInformation? = null
        var error: AccountRequestError? = null
        val response = client.postCatching<Pair<HttpResponse?, Exception?>> {
            url(baseUrl + CommonEndpoint.SIGNUP.url)
            contentType(ContentType.Application.Json)
            body = json.stringify(IOSSignupInformation.serializer(), information)
        }

        response.first?.let {
            if (AccountUtils.isErrorStatusCode(it.status.value)) {
                error = AccountRequestError(it.status.value, it.status.description)
            } else {
                it.content.readUTF8Line()?.let { content ->
                    signUpInformation = json.parse(SignUpInformation.serializer(), content)
                } ?: run {
                    error = AccountRequestError(600, "Request response undefined")
                }
            }
        }
        response.second?.let {
            error = AccountRequestError(600, it.message)
        }

        withContext(Dispatchers.Main) {
            callback(signUpInformation, error)
        }
    }

    private fun subscriptionsAsync(
        receipt: String?,
        callback: (details: IOSSubscriptionInformation?, error: AccountRequestError?) -> Unit
    ) = async {
        var subscriptionsInformation: IOSSubscriptionInformation? = null
        var error: AccountRequestError? = null
        val response = client.getCatching<Pair<HttpResponse?, Exception?>> {
            url(baseUrl + Endpoint.SUBSCRIPTIONS.url)
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
                    subscriptionsInformation = json.parse(IOSSubscriptionInformation.serializer(), content)
                } ?: run {
                    error = AccountRequestError(600, "Request response undefined")
                }
            }
        }
        response.second?.let {
            error = AccountRequestError(600, it.message)
        }

        withContext(Dispatchers.Main) {
            callback(subscriptionsInformation, error)
        }
    }
    // endregion
}