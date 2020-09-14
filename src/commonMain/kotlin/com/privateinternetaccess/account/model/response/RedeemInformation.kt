package com.privateinternetaccess.account.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class RedeemInformation(
    @SerialName("code")
    val message: String?,
    @SerialName("username")
    val username: String,
    @SerialName("password")
    val password: String
)