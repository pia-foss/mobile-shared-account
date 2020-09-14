package com.privateinternetaccess.account

import com.privateinternetaccess.account.internals.AndroidAccount
import com.privateinternetaccess.account.internals.IOSAccount
import com.privateinternetaccess.account.model.request.AndroidSignupInformation
import com.privateinternetaccess.account.model.request.IOSPaymentInformation
import com.privateinternetaccess.account.model.request.IOSSignupInformation
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
     * @param email `String`
     * @param callback `(error: AccountRequestError?) -> Unit`
     */
    fun loginLink(email: String, callback: (error: AccountRequestError?) -> Unit)

    /**
     * @param username `String`
     * @param password `String`
     * @param callback `(token: String?, error: AccountRequestError?) -> Unit`.
     */
    fun loginWithCredentials(
        username: String,
        password: String,
        callback: (token: String?, error: AccountRequestError?) -> Unit
    )

    /**
     * @param token `String`
     * @param callback `(error: AccountRequestError?) -> Unit`
     */
    fun logout(token: String, callback: (error: AccountRequestError?) -> Unit)

    /**
     * @param token `String`
     * @param callback `(details: AccountInformation, error: AccountRequestError?) -> Unit`
     */
    fun accountDetails(token: String, callback: (details: AccountInformation?, error: AccountRequestError?) -> Unit)

    /**
     * @param callback `(status: ClientStatusInformation?, error: AccountRequestError?) -> Unit`
     */
    fun clientStatus(callback: (status: ClientStatusInformation?, error: AccountRequestError?) -> Unit)

    /**
     * @param token `String`
     * @param email `String`
     * @param resetPassword `Boolean`
     * @param callback `(temporaryPassword: String, error: AccountRequestError?) -> Unit`
     */
    fun setEmail(
        token: String,
        email: String,
        resetPassword: Boolean,
        callback: (temporaryPassword: String?, error: AccountRequestError?) -> Unit
    )

    /**
     * @param token `String`
     * @param recipientEmail `String`
     * @param recipientName `String`
     * @param callback `(error: AccountRequestError?) -> Unit`
     */
    fun sendInvite(
        token: String,
        recipientEmail: String,
        recipientName: String,
        callback: (error: AccountRequestError?) -> Unit
    )

    /**
     * @param token `String`
     * @param callback `(details: InvitesDetailsInformation?, error: AccountRequestError?) -> Unit -> Unit`
     */
    fun invitesDetails(
        token: String,
        callback: (details: InvitesDetailsInformation?, error: AccountRequestError?) -> Unit
    )

    /**
     * @param email `String`
     * @param code `String`
     * @param callback `(details: RedeemInformation?, error: AccountRequestError?) -> Unit -> Unit`
     */
    fun redeem(
        email: String,
        code: String,
        callback: (details: RedeemInformation?, error: AccountRequestError?) -> Unit
    )

    /**
     * @return `Boolean`
     */
    fun isStaging(): Boolean
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
     * @param callback `(token: String?, error: AccountRequestError?) -> Unit`.
     */
    fun loginWithReceipt(
        store: String,
        token: String,
        productId: String,
        applicationPackage: String,
        callback: (token: String?, error: AccountRequestError?) -> Unit
    )

    /**
     * @param information `AndroidSignupInformation`
     * @param callback `(details: SignUpInformation?, error: AccountRequestError?) -> Unit`
     */
    fun signUp(
        information: AndroidSignupInformation,
        callback: (details: SignUpInformation?, error: AccountRequestError?) -> Unit
    )

    /**
     * @param callback `(details: AndroidSubscriptionsInformation?, error: AccountRequestError?) -> Unit`
     */
    fun subscriptions(callback: (details: AndroidSubscriptionsInformation?, error: AccountRequestError?) -> Unit)
}

/**
 * Interface defining the iOS specifics API deriving from the base one `AccountAPI`
 */
public interface IOSAccountAPI: AccountAPI {

    /**
     * @param receiptBase64 `String`
     * @param callback `(token: String?, error: AccountRequestError?) -> Unit`.
     */
    fun loginWithReceipt(
        receiptBase64: String,
        callback: (token: String?, error: AccountRequestError?) -> Unit
    )

    /**
     * @param username `String`
     * @param password `String`
     * @param email `String`
     * @param resetPassword `Boolean`
     * @param callback `(temporaryPassword: String, error: AccountRequestError?) -> Unit`
     */
    fun setEmail(
        username: String,
        password: String,
        email: String,
        resetPassword: Boolean,
        callback: (temporaryPassword: String?, error: AccountRequestError?) -> Unit
    )

    /**
     * @param username `String`
     * @param username `String`
     * @param information `IOSPaymentInformation`
     * @param callback `(error: AccountRequestError?) -> Unit`
     */
    fun payment(
        username: String,
        password: String,
        information: IOSPaymentInformation,
        callback: (error: AccountRequestError?) -> Unit
    )

    /**
     * @param information `IOSSignupInformation`
     * @param callback `(details: SignUpInformation?, error: AccountRequestError?) -> Unit`
     */
    fun signUp(
        information: IOSSignupInformation,
        callback: (details: SignUpInformation?, error: AccountRequestError?) -> Unit
    )

    /**
     * @param receipt `String?`
     * @param callback `(details: IOSSubscriptionInformation?, error: AccountRequestError?) -> Unit`
     */
    fun subscriptions(
        receipt: String?,
        callback: (details: IOSSubscriptionInformation?, error: AccountRequestError?) -> Unit
    )
}

/**
 * Builder class responsible for creating an instance of an object conforming to
 * either `AndroidAccountAPI` or `IOSAccountAPI` interface. Depending on the platform.
 */
public class AccountBuilder<T> {
    private var stagingEndpoint: String? = null
    private var platform: Platform? = null
    private var userAgentValue: String? = null

    /**
     * It sets the staging environment within the module.
     *
     * @param stagingEndpoint `String`.
     */
    fun setStagingEndpoint(stagingEndpoint: String): AccountBuilder<T> = apply { this.stagingEndpoint = stagingEndpoint }

    /**
     * It sets the platform for which we are building the API.
     *
     * @param platform `Platform`.
     */
    fun setPlatform(platform: Platform): AccountBuilder<T> = apply { this.platform = platform }

    /**
     * It sets the User-Agent value to be used in the requests.
     *
     * @param userAgentValue `String`.
     */
    fun setUserAgentValue(userAgentValue: String): AccountBuilder<T> = apply { this.userAgentValue = userAgentValue }

    /**
     * @return `AndroidAccountAPI` or `IOSAccountAPI` interface. Depending on the platform.
     */
    fun <T> build(): T {
        val platform = this.platform
            ?: throw Exception("Platform definition missing.")
        val userAgentValue = this.userAgentValue
            ?: throw Exception("User-Agent value missing.")
        return when (platform) {
            Platform.IOS -> IOSAccount(stagingEndpoint, userAgentValue) as T
            Platform.ANDROID -> AndroidAccount(stagingEndpoint, userAgentValue) as T
        }
    }
}

/**
 * Request error message containing the http code and description.
 */
public data class AccountRequestError(val code: Int, val message: String?)
