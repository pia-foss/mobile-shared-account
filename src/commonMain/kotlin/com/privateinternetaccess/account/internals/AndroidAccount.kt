package com.privateinternetaccess.account.internals

import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.Platform
import com.privateinternetaccess.account.internals.model.request.AndroidLoginReceiptRequest
import com.privateinternetaccess.account.internals.model.response.LoginResponse
import com.privateinternetaccess.account.internals.utils.AccountUtils
import com.privateinternetaccess.account.model.request.AndroidSignupInformation
import com.privateinternetaccess.account.model.response.AndroidSubscriptionsInformation
import com.privateinternetaccess.account.model.response.SignUpInformation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*

internal class AndroidAccount(
    stagingEndpoint: String?,
    userAgentValue: String
) : AndroidAccountAPI, Account(stagingEndpoint, Platform.ANDROID, userAgentValue) {

    private enum class Endpoint(val url: String) {
        SUBSCRIPTIONS("/api/client/android")
    }

    // region AndroidAccountAPI
    override fun loginWithReceipt(
        store: String,
        token: String,
        productId: String,
        applicationPackage: String,
        callback: (token: String?, error: AccountRequestError?) -> Unit
    ) {
        launch {
            loginWithReceiptAsync(store, token, productId, applicationPackage, callback)
        }
    }

    override fun signUp(
        information: AndroidSignupInformation,
        callback: (details: SignUpInformation?, error: AccountRequestError?) -> Unit
    ) {
        launch {
            signUpAsync(information, callback)
        }
    }

    override fun subscriptions(
        callback: (details: AndroidSubscriptionsInformation?, error: AccountRequestError?) -> Unit
    ) {
        launch {
            subscriptionsAsync(callback)
        }
    }
    // endregion

    // region private
    private fun loginWithReceiptAsync(
        store: String,
        token: String,
        productId: String,
        applicationPackage: String,
        callback: (token: String?, error: AccountRequestError?) -> Unit
    ) = async {
        var tokenResponse: String? = null
        var error: AccountRequestError? = null
        val receiptRequest = AndroidLoginReceiptRequest(
            store = store,
            receipt = AndroidLoginReceiptRequest.Receipt(
                token = token,
                productId = productId,
                applicationPackage = applicationPackage
            )
        )
        val response = client.postCatching<Pair<HttpResponse?, Exception?>> {
            url(baseUrl + CommonEndpoint.LOGIN.url)
            contentType(ContentType.Application.Json)
            body = json.stringify(AndroidLoginReceiptRequest.serializer(), receiptRequest)
        }

        response.first?.let {
            if (AccountUtils.isErrorStatusCode(it.status.value)) {
                error = AccountRequestError(it.status.value, it.status.description)
            } else {
                it.content.readUTF8Line()?.let { content ->
                    tokenResponse = json.parse(LoginResponse.serializer(), content).token
                } ?: run {
                    error = AccountRequestError(600, "Request response undefined")
                }
            }
        }
        response.second?.let {
            error = AccountRequestError(600, it.message)
        }

        withContext(Dispatchers.Main) {
            callback(tokenResponse, error)
        }
    }

    private fun signUpAsync(
        information: AndroidSignupInformation,
        callback: (details: SignUpInformation?, error: AccountRequestError?) -> Unit
    ) = async {
        var signUpInformation: SignUpInformation? = null
        var error: AccountRequestError? = null
        val response = client.postCatching<Pair<HttpResponse?, Exception?>> {
            url(baseUrl + CommonEndpoint.SIGNUP.url)
            contentType(ContentType.Application.Json)
            body = json.stringify(AndroidSignupInformation.serializer(), information)
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
        callback: (details: AndroidSubscriptionsInformation?, error: AccountRequestError?) -> Unit
    ) = async {
        var subscriptionsInformation: AndroidSubscriptionsInformation? = null
        var error: AccountRequestError? = null
        val response = client.getCatching<Pair<HttpResponse?, Exception?>> {
            url(baseUrl + Endpoint.SUBSCRIPTIONS.url)
        }

        response.first?.let {
            if (AccountUtils.isErrorStatusCode(it.status.value)) {
                error = AccountRequestError(it.status.value, it.status.description)
            } else {
                it.content.readUTF8Line()?.let { content ->
                    subscriptionsInformation = json.parse(AndroidSubscriptionsInformation.serializer(), content)
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