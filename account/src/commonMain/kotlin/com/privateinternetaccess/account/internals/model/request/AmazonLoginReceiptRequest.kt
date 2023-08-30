package com.privateinternetaccess.account.internals.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AmazonLoginReceiptRequest(
    @SerialName("receipt_id")
    val receiptId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("store")
    val store: String
)