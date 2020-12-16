package com.privateinternetaccess.account.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class DedicatedIPInformationResponse(
    @SerialName("result")
    val result: List<DedicatedIPInformation>
) {
    enum class Status {
        active,
        expired,
        invalid,
        error
    }

    @Serializable
    data class DedicatedIPInformation(
        @SerialName("id")
        val id: String? = null,
        @SerialName("ip")
        val ip: String? = null,
        @SerialName("cn")
        val cn: String? = null,
        @SerialName("groups")
        val groups: List<String>? = null,
        @SerialName("dip_expire")
        val dip_expire: Long? = null,
        @SerialName("dip_token")
        val dipToken: String,
        @SerialName("status")
        val status: Status
    )
}