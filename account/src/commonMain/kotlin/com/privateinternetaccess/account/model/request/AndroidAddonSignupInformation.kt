package com.privateinternetaccess.account.model.request


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AndroidAddonSignupInformation(
    @SerialName("receipt")
    val receipt: Receipt,
    @SerialName("store")
    val store: String
) {
    @Serializable
    data class Receipt(
        @SerialName("application_package")
        val applicationPackage: String,
        @SerialName("product_id")
        val productId: String,
        @SerialName("token")
        val token: String
    )
}