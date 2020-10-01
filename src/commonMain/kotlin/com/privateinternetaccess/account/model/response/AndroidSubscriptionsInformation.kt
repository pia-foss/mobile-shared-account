package com.privateinternetaccess.account.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AndroidSubscriptionsInformation(
    @SerialName("available_products")
    val availableProducts: List<AvailableProduct>,
    @SerialName("status")
    val status: String
) {
    @Serializable
    data class AvailableProduct(
        @SerialName("id")
        val id: String,
        @SerialName("legacy")
        val legacy: Boolean,
        @SerialName("plan")
        val plan: String,
        @SerialName("price")
        val price: String
    )
}