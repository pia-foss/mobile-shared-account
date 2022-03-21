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
import com.privateinternetaccess.account.internals.model.response.ApiTokenResponse
import com.privateinternetaccess.account.internals.utils.AccountUtils
import com.privateinternetaccess.account.model.request.AndroidSignupInformation
import com.privateinternetaccess.account.model.request.IOSSignupInformation
import com.privateinternetaccess.account.model.response.AndroidSubscriptionsInformation
import com.privateinternetaccess.account.model.response.SignUpInformation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.serialization.SerializationException


internal class AndroidAccount(
    endpointsProvider: IAccountEndpointProvider,
    certificate: String?,
    userAgentValue: String
) : AndroidAccountAPI, Account(endpointsProvider, certificate, userAgentValue, Platform.ANDROID) {

    // region AndroidAccountAPI
    override fun loginWithReceipt(
        store: String,
        token: String,
        productId: String,
        applicationPackage: String,
        callback: (error: List<AccountRequestError>) -> Unit
    ) {
        launch {
            loginWithReceiptAsync(store, token, productId, applicationPackage, endpointsProvider.accountEndpoints(), callback)
        }
    }

    override fun signUp(
        information: AndroidSignupInformation,
        callback: (details: SignUpInformation?, error: List<AccountRequestError>) -> Unit
    ) {
        launch {
            signUpAsync(information, endpointsProvider.accountEndpoints(), callback)
        }
    }

    override fun subscriptions(
        callback: (details: AndroidSubscriptionsInformation?, error: List<AccountRequestError>) -> Unit
    ) {
        launch {
            subscriptionsAsync(endpointsProvider.accountEndpoints(), callback)
        }
    }
    // endregion

    // region private
    private suspend fun loginWithReceiptAsync(
        store: String,
        token: String,
        productId: String,
        applicationPackage: String,
        endpoints: List<AccountEndpoint>,
        callback: (error: List<AccountRequestError>) -> Unit
    ) {
        val listErrors: MutableList<AccountRequestError> = mutableListOf()
        if (endpoints.isEmpty()) {
            listErrors.add(AccountRequestError(600, "No available endpoints to perform the request"))
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
            val receiptRequest = AndroidLoginReceiptRequest(
                store = store,
                receipt = AndroidLoginReceiptRequest.Receipt(
                    token = token,
                    productId = productId,
                    applicationPackage = applicationPackage
                )
            )
            val response = httpClient.postCatching<Pair<HttpResponse?, Exception?>> {
                url(url)
                contentType(ContentType.Application.Json)
                body = json.encodeToString(AndroidLoginReceiptRequest.serializer(), receiptRequest)
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(AccountRequestError(it.status.value, it.status.description))
                } else {
                    it.content.readUTF8Line()?.let { content ->
                        try {
                            val apiTokenResponse = json.decodeFromString(ApiTokenResponse.serializer(), content)
                            persistence.persistApiTokenResponse(apiTokenResponse)
                            refreshVpnToken(apiTokenResponse.apiToken, endpoints)
                            succeeded = true
                        } catch (exception: SerializationException) {
                            listErrors.add(AccountRequestError(600, "Decode error $exception"))
                        }
                    } ?: run {
                        listErrors.add(AccountRequestError(600, "Request response undefined"))
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
            callback(listErrors)
        }
    }

    private suspend fun signUpAsync(
        information: AndroidSignupInformation,
        endpoints: List<AccountEndpoint>,
        callback: (details: SignUpInformation?, error: List<AccountRequestError>) -> Unit
    ) {
        var signUpInformation: SignUpInformation? = null
        val listErrors: MutableList<AccountRequestError> = mutableListOf()
        if (endpoints.isEmpty()) {
            listErrors.add(AccountRequestError(600, "No available endpoints to perform the request"))
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

            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, Path.SIGNUP)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${Path.SIGNUP.url}"))
                continue
            }

            var succeeded = false
            val response = httpClient.postCatching<Pair<HttpResponse?, Exception?>> {
                url(url)
                contentType(ContentType.Application.Json)
                body = json.encodeToString(AndroidSignupInformation.serializer(), information)
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(AccountRequestError(it.status.value, it.status.description))
                } else {
                    it.content.readUTF8Line()?.let { content ->
                        try {
                            signUpInformation = json.decodeFromString(SignUpInformation.serializer(), content)
                            succeeded = true
                        } catch (exception: SerializationException) {
                            listErrors.add(AccountRequestError(600, "Decode error $exception"))
                        }
                    } ?: run {
                        listErrors.add(AccountRequestError(600, "Request response undefined"))
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
            callback(signUpInformation, listErrors)
        }
    }

    private suspend fun subscriptionsAsync(
        endpoints: List<AccountEndpoint>,
        callback: (details: AndroidSubscriptionsInformation?, error: List<AccountRequestError>) -> Unit
    ) {
        var subscriptionsInformation: AndroidSubscriptionsInformation? = null
        val listErrors: MutableList<AccountRequestError> = mutableListOf()
        if (endpoints.isEmpty()) {
            listErrors.add(AccountRequestError(600, "No available endpoints to perform the request"))
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

            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, Path.ANDROID_SUBSCRIPTIONS)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${Path.ANDROID_SUBSCRIPTIONS.url}"))
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
                    it.content.readUTF8Line()?.let { content ->
                        try {
                            subscriptionsInformation =
                                json.decodeFromString(AndroidSubscriptionsInformation.serializer(), content)
                            succeeded = true
                        } catch (exception: SerializationException) {
                            listErrors.add(AccountRequestError(600, "Decode error $exception"))
                        }
                    } ?: run {
                        listErrors.add(AccountRequestError(600, "Request response undefined"))
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
            callback(subscriptionsInformation, listErrors)
        }
    }
    // endregion
}