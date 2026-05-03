package com.example.assetlinkandroid.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val id: String,
    @SerialName("borrower_id") val borrowerId: String,
    @SerialName("inspector_id") val inspectorId: String? = null,
    @SerialName("storage_partner_id") val storagePartnerId: String? = null,
    val title: String,
    val description: String? = null,
    @SerialName("item_type") val itemType: String? = null,
    val make: String? = null,
    val model: String? = null,
    val defects: String? = null,
    val photos: List<String> = emptyList(),
    @SerialName("proof_of_ownership_url") val proofOfOwnershipUrl: String? = null,
    @SerialName("storage_location") val storageLocation: String? = null,
    @SerialName("reserve_amount") val reserveAmount: Double,
    @SerialName("interest_rate") val interestRate: Double,
    @SerialName("loan_duration_days") val loanDurationDays: Int,
    @SerialName("bidding_duration_hours") val biddingDurationHours: Int? = null,
    @SerialName("bidding_ends_at") val biddingEndsAt: String? = null,
    val status: ItemStatus = ItemStatus.PENDING,
    @SerialName("agreement_signed") val agreementSigned: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class ItemCategory(
    val id: String,
    val name: String,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

@Serializable
data class ItemSubcategory(
    val id: String,
    @SerialName("category_id") val categoryId: String,
    val name: String,
    @SerialName("sort_order") val sortOrder: Int = 0,
)
