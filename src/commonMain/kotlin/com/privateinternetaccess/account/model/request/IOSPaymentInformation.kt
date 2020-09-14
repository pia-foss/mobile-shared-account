package com.privateinternetaccess.account.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IOSPaymentInformation(
    @SerialName("store")
    private val store: String,
    @SerialName("receipt")
    val receipt: String,
    @SerialName("marketing")
    val marketing: String,
    @SerialName("debug")
    val debug: String
)