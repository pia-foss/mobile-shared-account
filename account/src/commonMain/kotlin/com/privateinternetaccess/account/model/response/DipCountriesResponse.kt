package com.privateinternetaccess.account.model.response


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DipCountriesResponse(
    @SerialName("dedicatedIpCountriesAvailable")
    val dedicatedIpCountriesAvailable: List<DedicatedIpCountriesAvailable>
) {
    @Serializable
    data class DedicatedIpCountriesAvailable(
        @SerialName("country_code")
        val countryCode: String,
        @SerialName("name")
        val name: String,
        @SerialName("new_regions")
        val newRegions: List<String>,
        @SerialName("regions")
        val regions: List<String>
    )
}