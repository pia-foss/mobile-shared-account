package com.privateinternetaccess.account.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClientStatusInformation(
    @SerialName("connected")
    val connected: Boolean,
    @SerialName("ip")
    val ip: String
)