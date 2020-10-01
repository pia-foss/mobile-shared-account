package com.privateinternetaccess.account.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountInformation(
    @SerialName("active")
    val active: Boolean,
    @SerialName("can_invite")
    val canInvite: Boolean,
    @SerialName("canceled")
    val canceled: Boolean,
    @SerialName("days_remaining")
    val daysRemaining: Int,
    @SerialName("email")
    val email: String,
    @SerialName("expiration_time")
    val expirationTime: Int,
    @SerialName("expire_alert")
    val expireAlert: Boolean,
    @SerialName("expired")
    val expired: Boolean,
    @SerialName("needs_payment")
    val needsPayment: Boolean,
    @SerialName("plan")
    val plan: String,
    @SerialName("product_id")
    val productId: String?,
    @SerialName("recurring")
    val recurring: Boolean,
    @SerialName("renew_url")
    val renewUrl: String,
    @SerialName("renewable")
    val renewable: Boolean,
    @SerialName("username")
    val username: String
)