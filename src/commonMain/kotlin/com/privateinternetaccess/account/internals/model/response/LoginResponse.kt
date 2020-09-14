package com.privateinternetaccess.account.internals.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class LoginResponse(
    @SerialName("token")
    val token: String
)