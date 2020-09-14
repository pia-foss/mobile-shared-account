package com.privateinternetaccess.account.internals.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AndroidLoginReceiptRequest(
    @SerialName("store")
    private val store: String,
    @SerialName("receipt")
    val receipt: Receipt
) {
    @Serializable
    data class Receipt(
        @SerialName("token")
        val token: String,
        @SerialName("product_id")
        val productId: String,
        @SerialName("application_package")
        val applicationPackage: String
    )
}