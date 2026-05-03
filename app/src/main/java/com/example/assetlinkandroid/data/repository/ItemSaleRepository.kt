package com.example.assetlinkandroid.data.repository

import com.example.assetlinkandroid.data.model.ItemSaleWithItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemSaleRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    private val bucket get() = supabase.storage.from("payment-proofs")

    /** Fetch sales with status listed or under_offer, joined with item details. */
    suspend fun listedSales(): List<ItemSaleWithItem> =
        supabase.from("item_sales").select(
            columns = Columns.raw("*, items(title, photos, item_type, description)")
        ) {
            filter { isIn("status", listOf("listed", "under_offer")) }
            order("created_at", Order.DESCENDING)
        }.decodeList()

    /** Buyer accepts a listed sale — sets buyer_id and status = under_offer. */
    suspend fun acceptSale(saleId: String, buyerId: String) {
        supabase.from("item_sales").update({
            set("buyer_id", buyerId)
            set("status", "under_offer")
        }) {
            filter {
                eq("id", saleId)
                eq("status", "listed")
            }
        }
    }

    /** Mark the parent item as under_offer. */
    suspend fun markItemUnderOffer(itemId: String) {
        supabase.from("items").update({
            set("status", "under_offer")
        }) {
            filter { eq("id", itemId) }
        }
    }

    /** Upload proof-of-payment image and return its public URL. */
    suspend fun uploadPop(saleId: String, bytes: ByteArray, ext: String): String {
        val path = "sales/$saleId/${System.currentTimeMillis()}.$ext"
        bucket.upload(path, bytes) { upsert = false }
        return bucket.publicUrl(path)
    }

    /** Submit the proof URL + note to the sale row, moving it to pending_approval. */
    suspend fun submitProof(saleId: String, proofUrl: String, note: String?) {
        supabase.from("item_sales").update({
            set("buyer_proof_url", proofUrl)
            set("buyer_note", note)
            set("status", "pending_approval")
        }) {
            filter {
                eq("id", saleId)
                eq("status", "under_offer")
            }
        }
    }
}
