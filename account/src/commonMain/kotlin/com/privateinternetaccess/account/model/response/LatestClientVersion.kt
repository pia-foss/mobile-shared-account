package com.privateinternetaccess.account.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LatestClientVersion(
    @SerialName("version_name")
    val versionName: String,
    @SerialName("version_code")
    val versionCode: String,
    val sha256: String,
    val url: String,
)