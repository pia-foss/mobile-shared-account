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

package com.privateinternetaccess.account

import com.privateinternetaccess.account.internals.AndroidAccount
import com.privateinternetaccess.account.internals.IOSAccount
import com.privateinternetaccess.account.model.request.AndroidSignupInformation
import com.privateinternetaccess.account.model.request.IOSPaymentInformation
import com.privateinternetaccess.account.model.request.IOSSignupInformation
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse.DedicatedIPInformation
import com.privateinternetaccess.account.model.response.*


/**
 * Enum containing the supported platforms.
 */
public enum class Platform {
    IOS,
    ANDROID
}

/**
 * Interface defining the base API for the supported platforms.
 */
public interface AccountAPI {

    /**
     * @return `String?`
     */
    fun apiToken(): String?

    /**
     * @return `String?`
     */
    fun vpnToken(): String?

    /**
     * @param apiToken `String`
     * @param callback `(error: List<AccountRequestError>) -> Unit`
     */
    fun migrateApiToken(apiToken: String, callback: (error: List<AccountRequestError>) -> Unit)

    /**
     * @param email `String`
     * @param callback `(error: List<AccountRequestError>) -> Unit`
     */
    fun loginLink(email: String, callback: (error: List<AccountRequestError>) -> Unit)

    /**
     * @param username `String`
     * @param password `String`
     * @param callback `(error: List<AccountRequestError>) -> Unit`
     */
    fun loginWithCredentials(
        username: String,
        password: String,
        callback: (error: List<AccountRequestError>) -> Unit
    )

    /**
     * @param callback `(error: List<AccountRequestError>) -> Unit`
     */
    fun logout(callback: (error: List<AccountRequestError>) -> Unit)

    /**
     * @param callback `(details: AccountInformation, error: List<AccountRequestError>) -> Unit`
     */
    fun accountDetails(callback: (details: AccountInformation?, error: List<AccountRequestError>) -> Unit)

    /**
     * @param callback `(error: List<AccountRequestError>) -> Unit`
     */
    fun deleteAccount(callback: (error: List<AccountRequestError>) -> Unit)

    /**
     * @param ipTokens `List<String>`
     * @param callback `(details: DedicatedIPInformation, error: List<AccountRequestError>) -> Unit`
     */
    fun dedicatedIPs(
        ipTokens: List<String>,
        callback: (details: List<DedicatedIPInformation>, error: List<AccountRequestError>) -> Unit
    )

    /**
     * @param ipToken `String`
     * @param callback `(details: DedicatedIPInformation, error: List<AccountRequestError>) -> Unit`
     */
    fun renewDedicatedIP(
        ipToken: String,
        callback: (error: List<AccountRequestError>) -> Unit
    )

    /**
     * @param callback `(status: ClientStatusInformation?, error: List<AccountRequestError>) -> Unit`
     */
    fun clientStatus(callback: (status: ClientStatusInformation?, error: List<AccountRequestError>) -> Unit)

    /**
     * @param email `String`
     * @param resetPassword `Boolean`
     * @param callback `(temporaryPassword: String, error: List<AccountRequestError>) -> Unit`
     */
    fun setEmail(
        email: String,
        resetPassword: Boolean,
        callback: (temporaryPassword: String?, error: List<AccountRequestError>) -> Unit
    )

    /**
     * @param recipientEmail `String`
     * @param recipientName `String`
     * @param callback `(error: List<AccountRequestError>) -> Unit`
     */
    fun sendInvite(
        recipientEmail: String,
        recipientName: String,
        callback: (error: List<AccountRequestError>) -> Unit
    )

    /**
     * @param callback `(details: InvitesDetailsInformation?, error: List<AccountRequestError>) -> Unit -> Unit`
     */
    fun invitesDetails(callback: (details: InvitesDetailsInformation?, error: List<AccountRequestError>) -> Unit)

    /**
     * @param email `String`
     * @param code `String`
     * @param callback `(details: RedeemInformation?, error: List<AccountRequestError>) -> Unit`
     */
    fun redeem(
        email: String,
        code: String,
        callback: (details: RedeemInformation?, error: List<AccountRequestError>) -> Unit
    )

    /**
     * It returns an in-app communication message for each platform.
     *
     * @param appVersion `String`
     * @param callback `(message: MessageInformation?, error: List<AccountRequestError>) -> Unit`
     */
    fun message(appVersion: String, callback: (message: MessageInformation?, error: List<AccountRequestError>) -> Unit)

    /**
     * @param callback `(details: FeatureFlagsInformation?, error: List<AccountRequestError>) -> Unit`
     */
    fun featureFlags(
        callback: (details: FeatureFlagsInformation?, error: List<AccountRequestError>) -> Unit
    )
}

/**
 * Interface defining the Android specifics API deriving from the base one `AccountAPI`
 */
public interface AndroidAccountAPI: AccountAPI {

    /**
     * @param store `String`
     * @param token `String`
     * @param productId `String`
     * @param applicationPackage `String`
     * @param callback `(error:List<AccountRequestError>) -> Unit`.
     */
    fun loginWithReceipt(
        store: String,
        token: String,
        productId: String,
        applicationPackage: String,
        callback: (error: List<AccountRequestError>) -> Unit
    )

    /**
     * @param information `AndroidSignupInformation`
     * @param callback `(details: SignUpInformation?, error: List<AccountRequestError>) -> Unit`
     */
    fun signUp(
        information: AndroidSignupInformation,
        callback: (details: SignUpInformation?, error: List<AccountRequestError>) -> Unit
    )

    /**
     * @param callback `(details: AndroidSubscriptionsInformation?, error: List<AccountRequestError>) -> Unit`
     */
    fun subscriptions(callback: (details: AndroidSubscriptionsInformation?, error: List<AccountRequestError>) -> Unit)
}

/**
 * Interface defining the iOS specifics API deriving from the base one `AccountAPI`
 */
public interface IOSAccountAPI: AccountAPI {

    /**
     * @param receiptBase64 `String`
     * @param callback `(error: List<AccountRequestError>) -> Unit`.
     */
    fun loginWithReceipt(
        receiptBase64: String,
        callback: (error: List<AccountRequestError>) -> Unit
    )

    /**
     * @param username `String`
     * @param password `String`
     * @param email `String`
     * @param resetPassword `Boolean`
     * @param callback `(temporaryPassword: String, error: List<AccountRequestError>) -> Unit`
     */
    fun setEmail(
        username: String,
        password: String,
        email: String,
        resetPassword: Boolean,
        callback: (temporaryPassword: String?, error: List<AccountRequestError>) -> Unit
    )

    /**
     * @param username `String`
     * @param username `String`
     * @param information `IOSPaymentInformation`
     * @param callback `(error: List<AccountRequestError>) -> Unit`
     */
    fun payment(
        username: String,
        password: String,
        information: IOSPaymentInformation,
        callback: (error: List<AccountRequestError>) -> Unit
    )

    /**
     * @param information `IOSSignupInformation`
     * @param callback `(details: SignUpInformation?, error: List<AccountRequestError>) -> Unit`
     */
    fun signUp(
        information: IOSSignupInformation,
        callback: (details: SignUpInformation?, error: List<AccountRequestError>) -> Unit
    )

    /**
     * @param receipt `String?`
     * @param callback `(details: IOSSubscriptionInformation?, error: List<AccountRequestError>) -> Unit`
     */
    fun subscriptions(
        receipt: String?,
        callback: (details: IOSSubscriptionInformation?, error: List<AccountRequestError>) -> Unit
    )
}

/**
 * Interface defining the client's endpoint provider.
 */
public interface IAccountEndpointProvider {

    /**
     * It returns the list of endpoints to try to reach when performing a request. Order is relevant.
     *
     * @return `List<AccountEndpoint>`
     */
    fun accountEndpoints(): List<AccountEndpoint>
}

/**
 * Builder class responsible for creating an instance of an object conforming to
 * either `AndroidAccountAPI` or `IOSAccountAPI` interface. Depending on the platform.
 */
public class AccountBuilder<T> {
    private var endpointsProvider: IAccountEndpointProvider? = null
    private var certificate: String? = null
    private var platform: Platform? = null
    private var userAgentValue: String? = null

    /**
     * It sets the endpoints provider, that is queried for the current endpoint list. Required.
     *
     * @param endpointsProvider `IAccountEndpointProvider`.
     */
    fun setEndpointProvider(endpointsProvider: IAccountEndpointProvider): AccountBuilder<T> =
        apply { this.endpointsProvider = endpointsProvider }

    /**
     * It sets the certificate to use when using an endpoint with pinning enabled. Optional.
     *
     * @param certificate `String`.
     */
    fun setCertificate(certificate: String?): AccountBuilder<T> = apply {
        this.certificate = certificate
    }

    /**
     * It sets the platform for which we are building the API.
     *
     * @param platform `Platform`.
     */
    fun setPlatform(platform: Platform): AccountBuilder<T> =
        apply { this.platform = platform }

    /**
     * It sets the User-Agent value to be used in the requests.
     *
     * @param userAgentValue `String`.
     */
    fun setUserAgentValue(userAgentValue: String): AccountBuilder<T> =
        apply { this.userAgentValue = userAgentValue }

    /**
     * @return `AndroidAccountAPI` or `IOSAccountAPI` interface. Depending on the platform.
     */
    fun <T> build(): T {
        val endpointsProvider = this.endpointsProvider
            ?: throw Exception("Endpoints provider missing.")
        val platform = this.platform
            ?: throw Exception("Platform definition missing.")
        val userAgentValue = this.userAgentValue
            ?: throw Exception("User-Agent value missing.")
        return when (platform) {
            Platform.IOS -> IOSAccount(endpointsProvider, certificate, userAgentValue) as T
            Platform.ANDROID -> AndroidAccount(endpointsProvider, certificate, userAgentValue) as T
        }
    }
}

/**
 * Request error message containing the http code and description.
 */
public data class AccountRequestError(val code: Int, val message: String?)

/**
 * Data class defining the endpoints data needed when performing a request on it.
 *
 * @param ipOrRootDomain `String`. Indicates the ip or root domain to use for the requests.
 * e.g. `127.0.0.1` or `privateinternetaccess.com`
 * @param isProxy `Boolean`. Indicates whether the given address should be treated as a proxy. Excluding it from
 * proxy sensitive request. e.g. showing the user its ip.
 * @param usePinnedCertificate `Boolean`. Indicates whether this address should be pinned to the provided certificate.
 * @param certificateCommonName `String?`. When pinning is enabled. Provide the common name for it.
 */

public data class AccountEndpoint(
    val ipOrRootDomain: String,
    val isProxy: Boolean,
    val usePinnedCertificate: Boolean = false,
    val certificateCommonName: String? = null
)
