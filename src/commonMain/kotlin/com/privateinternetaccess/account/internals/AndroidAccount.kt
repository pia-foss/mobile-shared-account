package com.privateinternetaccess.account.internals

import com.privateinternetaccess.account.*
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
    clientStateProvider: AccountClientStateProvider,
    userAgentValue: String
) : AndroidAccountAPI, Account(clientStateProvider, userAgentValue) {

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
            loginWithReceiptAsync(store, token, productId, applicationPackage, clientStateProvider.accountEndpoints(), callback)
        }
    }

    override fun signUp(
        information: AndroidSignupInformation,
        callback: (details: SignUpInformation?, error: AccountRequestError?) -> Unit
    ) {
        launch {
            signUpAsync(information, clientStateProvider.accountEndpoints(), callback)
        }
    }

    override fun subscriptions(
        callback: (details: AndroidSubscriptionsInformation?, error: AccountRequestError?) -> Unit
    ) {
        launch {
            subscriptionsAsync(clientStateProvider.accountEndpoints(), callback)
        }
    }
    // endregion

    // region private
    private fun loginWithReceiptAsync(
        store: String,
        token: String,
        productId: String,
        applicationPackage: String,
        endpoints: List<AccountEndpoint>,
        callback: (token: String?, error: AccountRequestError?) -> Unit
    ) = async {
        for (accountEndpoint in endpoints) {
            var tokenResponse: String? = null
            var error: AccountRequestError? = null
            var subdomain: String?
            val client = if (accountEndpoint.usePinnedCertificate) {
                subdomain = CommonMetaEndpoint.LOGIN.url
                AccountHttpClient.client(Pair(accountEndpoint.endpoint, accountEndpoint.certificateCommonName!!))
            } else {
                subdomain = CommonEndpoint.LOGIN.url
                AccountHttpClient.client()
            }
            val receiptRequest = AndroidLoginReceiptRequest(
                store = store,
                receipt = AndroidLoginReceiptRequest.Receipt(
                    token = token,
                    productId = productId,
                    applicationPackage = applicationPackage
                )
            )
            val response = client.postCatching<Pair<HttpResponse?, Exception?>> {
                url("https://${accountEndpoint.endpoint}$subdomain")
                contentType(ContentType.Application.Json)
                body = json.encodeToString(AndroidLoginReceiptRequest.serializer(), receiptRequest)
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    error = AccountRequestError(it.status.value, it.status.description)
                } else {
                    it.content.readUTF8Line()?.let { content ->
                        tokenResponse = json.decodeFromString(LoginResponse.serializer(), content).token
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
                callback(tokenResponse, error)
            }
            break
        }
    }

    private fun signUpAsync(
        information: AndroidSignupInformation,
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
                body = json.encodeToString(AndroidSignupInformation.serializer(), information)
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
        endpoints: List<AccountEndpoint>,
        callback: (details: AndroidSubscriptionsInformation?, error: AccountRequestError?) -> Unit
    ) = async {
        for (accountEndpoint in endpoints) {
            var subscriptionsInformation: AndroidSubscriptionsInformation? = null
            var error: AccountRequestError? = null
            val client = if (accountEndpoint.usePinnedCertificate) {
                AccountHttpClient.client(Pair(accountEndpoint.endpoint, accountEndpoint.certificateCommonName!!))
            } else {
                AccountHttpClient.client()
            }
            val response = client.getCatching<Pair<HttpResponse?, Exception?>> {
                url("https://${accountEndpoint.endpoint}${Endpoint.SUBSCRIPTIONS.url}")
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    error = AccountRequestError(it.status.value, it.status.description)
                } else {
                    it.content.readUTF8Line()?.let { content ->
                        subscriptionsInformation =
                            json.decodeFromString(AndroidSubscriptionsInformation.serializer(), content)
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