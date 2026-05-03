package com.example.assetlinkandroid.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Offer(
    val id: String,
    @SerialName("item_id") val itemId: String,
    @SerialName("lender_id") val lenderId: String,
    val amount: Double,
    @SerialName("proposed_interest") val proposedInterest: Double,
    val message: String? = null,
    val status: OfferStatus = OfferStatus.PENDING,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class NewOffer(
    @SerialName("item_id") val itemId: String,
    @SerialName("lender_id") val lenderId: String,
    val amount: Double,
    @SerialName("proposed_interest") val proposedInterest: Double,
    val message: String? = null,
)
