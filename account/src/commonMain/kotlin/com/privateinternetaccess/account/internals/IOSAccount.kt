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
import com.privateinternetaccess.account.Platform
import com.privateinternetaccess.account.internals.model.request.IOSLoginReceiptRequest
import com.privateinternetaccess.account.internals.model.response.ApiTokenResponse
import com.privateinternetaccess.account.internals.model.response.SetEmailResponse
import com.privateinternetaccess.account.internals.utils.AccountUtils
import com.privateinternetaccess.account.internals.utils.NetworkUtils.mapStatusCodeToAccountError
import com.privateinternetaccess.account.model.request.IOSPaymentInformation
import com.privateinternetaccess.account.model.request.IOSSignupInformation
import com.privateinternetaccess.account.model.response.IOSSubscriptionInformation
import com.privateinternetaccess.account.model.response.VpnSignUpInformation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.serialization.SerializationException


internal class IOSAccount(
    endpointsProvider: IAccountEndpointProvider,
    certificate: String?,
    userAgentValue: String
) : IOSAccountAPI, Account(endpointsProvider, certificate, userAgentValue, Platform.IOS) {

    @InternalAPI
    override fun loginWithReceipt(
        receiptBase64: String,
        callback: (error: List<AccountRequestError>) -> Unit
    ) {
        launch {
            loginWithReceiptAsync(receiptBase64, endpointsProvider.accountEndpoints(), callback)
        }
    }

    @InternalAPI
    override fun setEmail(
        username: String,
        password: String,
        email: String,
        resetPassword: Boolean,
        callback: (temporaryPassword: String?, error: List<AccountRequestError>) -> Unit
    ) {
        launch {
            setEmailAsync(username, password, email, resetPassword, endpointsProvider.accountEndpoints(), callback)
        }
    }

    @InternalAPI
    override fun payment(
        username: String,
        password: String,
        information: IOSPaymentInformation,
        callback: (error: List<AccountRequestError>) -> Unit
    ) {
        launch {
            paymentAsync(username, password, information, endpointsProvider.accountEndpoints(), callback)
        }
    }

    override fun signUp(
        information: IOSSignupInformation,
        callback: (details: VpnSignUpInformation?, error: List<AccountRequestError>) -> Unit
    ) {
        launch {
            signUpAsync(information, endpointsProvider.accountEndpoints(), callback)
        }
    }

    override fun subscriptions(
        receipt: String?,
        callback: (details: IOSSubscriptionInformation?, error: List<AccountRequestError>) -> Unit
    ) {
        launch {
            subscriptionsAsync(receipt, endpointsProvider.accountEndpoints(), callback)
        }
    }
    @InternalAPI
    override fun validateLoginQR(
        qrToken: String,
        callback: (apiToken: String?, error: List<AccountRequestError>) -> Unit
    ) {
        launch {
            validateLoginQRAsync(qrToken, endpointsProvider.accountEndpoints(), callback)
        }
    }
    // endregion

    // region private
    @InternalAPI
    private suspend fun validateLoginQRAsync(
        qrToken: String,
        endpoints: List<AccountEndpoint>,
        callback: (apiToken: String?, error: List<AccountRequestError>) -> Unit
    ) {
        var apiToken: String? = null
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

            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, Path.VALIDATE_QR)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${Path.VALIDATE_QR.url}"))
                continue
            }

            var succeeded = false
            val response = httpClient.postCatching<Pair<HttpResponse?, Exception?>> {
                url(url)
                header("Authorization", "Bearer $qrToken")
                header("accept", "application/json")
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(it.mapStatusCodeToAccountError())
                } else {
                    it.content.readUTF8Line()?.let { content ->
                        try {
                            val apiTokenResponse = json.decodeFromString(ApiTokenResponse.serializer(), content)
                            apiToken = apiTokenResponse.apiToken
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
            callback(apiToken, listErrors)
        }
    }
    @InternalAPI
    private suspend fun loginWithReceiptAsync(
        receiptBase64: String,
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
            val response = httpClient.postCatching<Pair<HttpResponse?, Exception?>> {
                url(url)
                contentType(ContentType.Application.Json)
                body = json.encodeToString(IOSLoginReceiptRequest.serializer(), IOSLoginReceiptRequest(receiptBase64))
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(it.mapStatusCodeToAccountError())
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

    @InternalAPI
    private suspend fun setEmailAsync(
        username: String,
        password: String,
        email: String,
        resetPassword: Boolean,
        endpoints: List<AccountEndpoint>,
        callback: (temporaryPassword: String?, error: List<AccountRequestError>) -> Unit
    ) {
        var temporaryPassword: String? = null
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

            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, Path.SET_EMAIL)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${Path.SET_EMAIL.url}"))
                continue
            }

            var succeeded = false
            val auth = "$username:$password".encodeBase64()
            val response = httpClient.postCatching<Pair<HttpResponse?, Exception?>> {
                url(url)
                header("Authorization", "Basic $auth")
                parameter("email", email)
                parameter("reset_password", resetPassword)
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(AccountRequestError(it.status.value, it.status.description))
                } else {
                    it.content.readUTF8Line()?.let { content ->
                        try {
                            temporaryPassword = json.decodeFromString(SetEmailResponse.serializer(), content).password
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
            callback(temporaryPassword, listErrors)
        }
    }

    @InternalAPI
    private suspend fun paymentAsync(
        username: String,
        password: String,
        information: IOSPaymentInformation,
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

            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, Path.IOS_PAYMENT)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${Path.IOS_PAYMENT.url}"))
                continue
            }

            var succeeded = false
            val auth = "$username:$password".encodeBase64()
            val response = httpClient.postCatching<Pair<HttpResponse?, Exception?>> {
                url(url)
                contentType(ContentType.Application.Json)
                header("Authorization", "Basic $auth")
                body = json.encodeToString(IOSPaymentInformation.serializer(), information)
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

    private suspend fun signUpAsync(
        information: IOSSignupInformation,
        endpoints: List<AccountEndpoint>,
        callback: (details: VpnSignUpInformation?, error: List<AccountRequestError>) -> Unit
    ) {
        var signUpInformation: VpnSignUpInformation? = null
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

            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, Path.VPN_SIGNUP)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${Path.VPN_SIGNUP.url}"))
                continue
            }

            var succeeded = false
            val response = httpClient.postCatching<Pair<HttpResponse?, Exception?>> {
                url(url)
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(IOSSignupInformation.serializer(), information))
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(AccountRequestError(it.status.value, it.status.description))
                } else {
                    try {
                        signUpInformation = json.decodeFromString(VpnSignUpInformation.serializer(), it.bodyAsText())
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
            callback(signUpInformation, listErrors)
        }
    }

    private suspend fun subscriptionsAsync(
        receipt: String?,
        endpoints: List<AccountEndpoint>,
        callback: (details: IOSSubscriptionInformation?, error: List<AccountRequestError>) -> Unit
    ) {
        var subscriptionsInformation: IOSSubscriptionInformation? = null
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

            val url = AccountUtils.prepareRequestUrl(endpoint.ipOrRootDomain, Path.IOS_SUBSCRIPTIONS)
            if (url == null) {
                listErrors.add(AccountRequestError(600, "Error preparing url ${endpoint.ipOrRootDomain} - ${Path.IOS_SUBSCRIPTIONS.url}"))
                continue
            }

            var succeeded = false
            val response = httpClient.getCatching<Pair<HttpResponse?, Exception?>> {
                url(url)
                parameter("type", "subscription")
                if (receipt != null) {
                    parameter("receipt", receipt)
                }
            }

            response.first?.let {
                if (AccountUtils.isErrorStatusCode(it.status.value)) {
                    listErrors.add(AccountRequestError(it.status.value, it.status.description))
                } else {
                    try {
                        subscriptionsInformation =
                            json.decodeFromString(IOSSubscriptionInformation.serializer(), it.bodyAsText())
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
            callback(subscriptionsInformation, listErrors)
        }
    }
    // endregion
}