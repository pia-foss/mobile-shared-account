package com.privateinternetaccess.account.internals.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SetEmailResponse(
    @SerialName("password")
    val password: String = "",
    @SerialName("status")
    val status: String
)