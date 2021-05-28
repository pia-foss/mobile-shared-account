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
import com.privateinternetaccess.account.internals.model.request.AndroidLoginReceiptRequest
import com.privateinternetaccess.account.model.response.LoginResponse
import com.privateinternetaccess.account.internals.utils.AccountUtils
import com.privateinternetaccess.account.model.request.AndroidSignupInformation
import com.privateinternetaccess.account.model.response.AndroidSubscriptionsInformation
import com.privateinternetaccess.account.model.response.SignUpInformation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.serialization.SerializationException


internal class AndroidAccount(
    clientStateProvider: AccountClientStateProvider,
    userAgentValue: String
) : AndroidAccountAPI, Account(clientStateProvider, userAgentValue, Platform.ANDROID) {

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
        var tokenResponse: String? = null
        var error: AccountRequestError? = null
        if (endpoints.isNullOrEmpty()) {
            error = AccountRequestError(600, "No available endpoints to perform the request")
        }

        for (accountEndpoint in endpoints) {
            error = null
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
                        try {
                            tokenResponse = json.decodeFromString(LoginResponse.serializer(), content).token
                        } catch (exception: SerializationException) {
                            error = AccountRequestError(600, "Decode error $exception")
                        }
                    } ?: run {
                        error = AccountRequestError(600, "Request response undefined")
                    }
                }
            }
            response.second?.let {
                error = AccountRequestError(600, it.message)
            }

            // If there were no errors in the request for the current endpoint. No need to try the next endpoint.
            if (error == null) {
                break
            }
        }

        withContext(Dispatchers.Main) {
            callback(tokenResponse, error)
        }
    }

    private fun signUpAsync(
        information: AndroidSignupInformation,
        endpoints: List<AccountEndpoint>,
        callback: (details: SignUpInformation?, error: AccountRequestError?) -> Unit
    ) = async {
        var signUpInformation: SignUpInformation? = null
        var error: AccountRequestError? = null
        if (endpoints.isNullOrEmpty()) {
            error = AccountRequestError(600, "No available endpoints to perform the request")
        }

        for (accountEndpoint in endpoints) {
            error = null
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
                        try {
                            signUpInformation = json.decodeFromString(SignUpInformation.serializer(), content)
                        } catch (exception: SerializationException) {
                            error = AccountRequestError(600, "Decode error $exception")
                        }
                    } ?: run {
                        error = AccountRequestError(600, "Request response undefined")
                    }
                }
            }
            response.second?.let {
                error = AccountRequestError(600, it.message)
            }

            // If there were no errors in the request for the current endpoint. No need to try the next endpoint.
            if (error == null) {
                break
            }
        }

        withContext(Dispatchers.Main) {
            callback(signUpInformation, error)
        }
    }

    private fun subscriptionsAsync(
        endpoints: List<AccountEndpoint>,
        callback: (details: AndroidSubscriptionsInformation?, error: AccountRequestError?) -> Unit
    ) = async {
        var subscriptionsInformation: AndroidSubscriptionsInformation? = null
        var error: AccountRequestError? = null
        if (endpoints.isNullOrEmpty()) {
            error = AccountRequestError(600, "No available endpoints to perform the request")
        }

        for (accountEndpoint in endpoints) {
            error = null
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
                        try {
                            subscriptionsInformation =
                                json.decodeFromString(AndroidSubscriptionsInformation.serializer(), content)
                        } catch (exception: SerializationException) {
                            error = AccountRequestError(600, "Decode error $exception")
                        }
                    } ?: run {
                        error = AccountRequestError(600, "Request response undefined")
                    }
                }
            }
            response.second?.let {
                error = AccountRequestError(600, it.message)
            }

            // If there were no errors in the request for the current endpoint. No need to try the next endpoint.
            if (error == null) {
                break
            }
        }

        withContext(Dispatchers.Main) {
            callback(subscriptionsInformation, error)
        }
    }
    // endregion
}