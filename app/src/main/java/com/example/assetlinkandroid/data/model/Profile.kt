package com.example.assetlinkandroid.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    @SerialName("full_name") val fullName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("kyc_verified") val kycVerified: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class UserRoleRow(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    val role: AppRole,
    @SerialName("created_at") val createdAt: String? = null,
)
