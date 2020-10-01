package com.privateinternetaccess.account.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AndroidSignupInformation(
    @SerialName("store")
    private val store: String,
    @SerialName("receipt")
    val receipt: Receipt,
    @SerialName("marketing")
    val marketing: String? = null
) {
    @Serializable
    data class Receipt(
        @SerialName("order_id")
        val orderId: String,
        @SerialName("token")
        val token: String,
        @SerialName("product_id")
        val sku: String
    )
}