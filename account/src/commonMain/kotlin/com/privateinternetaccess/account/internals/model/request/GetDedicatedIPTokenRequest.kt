package com.privateinternetaccess.account.internals.model.request


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetDedicatedIPTokenRequest(
    @SerialName("country_code")
    val countryCode: String,
    @SerialName("region")
    val region: String
)