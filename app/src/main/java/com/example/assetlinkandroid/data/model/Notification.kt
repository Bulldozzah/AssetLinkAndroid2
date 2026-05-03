package com.example.assetlinkandroid.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppNotification(
    val id: String,
    @SerialName("user_id") val userId: String,
    val title: String,
    val body: String? = null,
    val link: String? = null,
    val read: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class NewAppNotification(
    @SerialName("user_id") val userId: String,
    val title: String,
    val body: String? = null,
    val link: String? = null,
)
