package com.example.assetlinkandroid.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mirrors the `item_sales` table row with a joined `items(title, photos, item_type, description)`.
 * The nested [items] field is populated by the Supabase embedded-select.
 */
@Serializable
data class ItemSaleWithItem(
    val id: String,
    @SerialName("item_id") val itemId: String,
    @SerialName("loan_id") val loanId: String,
    @SerialName("flagged_by") val flaggedBy: String,
    @SerialName("sale_price") val salePrice: Double,
    @SerialName("buyer_id") val buyerId: String? = null,
    val status: String = "listed",
    @SerialName("buyer_proof_url") val buyerProofUrl: String? = null,
    @SerialName("buyer_note") val buyerNote: String? = null,
    @SerialName("reviewer_note") val reviewerNote: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val items: ItemSaleJoinedItem? = null,
)

@Serializable
data class ItemSaleJoinedItem(
    val title: String = "",
    val photos: List<String> = emptyList(),
    @SerialName("item_type") val itemType: String? = null,
    val description: String? = null,
)

/** Flat convenience view used by the UI layer. */
data class SaleListing(
    val id: String,
    val itemId: String,
    val loanId: String,
    val flaggedBy: String,
    val salePrice: Double,
    val buyerId: String?,
    val status: String,
    val buyerProofUrl: String?,
    val buyerNote: String?,
    val reviewerNote: String?,
    val createdAt: String?,
    val itemTitle: String,
    val itemPhotos: List<String>,
    val itemType: String?,
    val itemDescription: String?,
)

fun ItemSaleWithItem.toSaleListing() = SaleListing(
    id = id,
    itemId = itemId,
    loanId = loanId,
    flaggedBy = flaggedBy,
    salePrice = salePrice,
    buyerId = buyerId,
    status = status,
    buyerProofUrl = buyerProofUrl,
    buyerNote = buyerNote,
    reviewerNote = reviewerNote,
    createdAt = createdAt,
    itemTitle = items?.title ?: "Untitled",
    itemPhotos = items?.photos ?: emptyList(),
    itemType = items?.itemType,
    itemDescription = items?.description,
)
