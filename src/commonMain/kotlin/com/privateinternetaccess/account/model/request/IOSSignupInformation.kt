package com.privateinternetaccess.account.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IOSSignupInformation(
    @SerialName("store")
    private val store: String,
    @SerialName("receipt")
    val receipt: String,
    @SerialName("email")
    val email: String,
    @SerialName("marketing")
    val marketing: String? = null,
    @SerialName("debug")
    val debug: String? = null
)