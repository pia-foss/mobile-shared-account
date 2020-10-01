package com.privateinternetaccess.account.internals.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class IOSLoginReceiptRequest(
    @SerialName("receipt")
    val receipt: String
)