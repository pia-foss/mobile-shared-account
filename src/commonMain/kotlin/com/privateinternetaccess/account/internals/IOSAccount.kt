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
import kotlinx.serialization.SerializationException


internal class IOSAccount(
    clientStateProvider: AccountClientStateProvider,
    userAgentValue: String
) : IOSAccountAPI, Account(clientStateProvider, userAgentValue, Platform.IOS) {

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
        var token: String? = null
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
                        try {
                            token = json.decodeFromString(LoginResponse.serializer(), content).token
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
            callback(token, error)
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
        var temporaryPassword: String? = null
        var error: AccountRequestError? = null
        if (endpoints.isNullOrEmpty()) {
            error = AccountRequestError(600, "No available endpoints to perform the request")
        }

        for (accountEndpoint in endpoints) {
            error = null
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
                        try {
                            temporaryPassword = json.decodeFromString(SetEmailResponse.serializer(), content).password
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
            callback(temporaryPassword, error)
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
        var error: AccountRequestError? = null
        if (endpoints.isNullOrEmpty()) {
            error = AccountRequestError(600, "No available endpoints to perform the request")
        }

        for (accountEndpoint in endpoints) {
            error = null
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

            // If there were no errors in the request for the current endpoint. No need to try the next endpoint.
            if (error == null) {
                break
            }
        }

        withContext(Dispatchers.Main) {
            callback(error)
        }
    }

    private fun signUpAsync(
        information: IOSSignupInformation,
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
                body = json.encodeToString(IOSSignupInformation.serializer(), information)
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
        receipt: String?,
        endpoints: List<AccountEndpoint>,
        callback: (details: IOSSubscriptionInformation?, error: AccountRequestError?) -> Unit
    ) = async {
        var subscriptionsInformation: IOSSubscriptionInformation? = null
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
                        try {
                            subscriptionsInformation =
                                json.decodeFromString(IOSSubscriptionInformation.serializer(), content)
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