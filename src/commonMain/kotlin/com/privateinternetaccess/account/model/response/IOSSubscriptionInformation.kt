package com.privateinternetaccess.account.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IOSSubscriptionInformation(
    @SerialName("available_products")
    val availableProducts: List<AvailableProduct>,
    @SerialName("eligible_for_trial")
    val eligibleForTrial: Boolean,
    @SerialName("receipt")
    val receipt: Receipt,
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

    @Serializable
    data class Receipt(
        @SerialName("eligible_for_trial")
        val eligibleForTrial: Boolean
    )
}