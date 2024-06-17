package com.privateinternetaccess.account.model.response


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DedicatedIPTokenDetails(
    @SerialName("meta_data")
    val metaData: List<MetaData>,
    @SerialName("partners_id")
    val partnersId: Int,
    @SerialName("redeemed_at")
    val redeemedAt: String,
    @SerialName("token")
    val token: String
) {
    @Serializable
    data class MetaData(
        @SerialName("common_name")
        val commonName: String,
        @SerialName("region_id")
        val regionId: String
    )
}