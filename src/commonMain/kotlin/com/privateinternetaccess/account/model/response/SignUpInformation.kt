package com.privateinternetaccess.account.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class SignUpInformation(
    @SerialName("status")
    val status: String,
    @SerialName("username")
    val username: String,
    @SerialName("password")
    val password: String
)