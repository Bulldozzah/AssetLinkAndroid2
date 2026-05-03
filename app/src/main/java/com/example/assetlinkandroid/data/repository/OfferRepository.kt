package com.example.assetlinkandroid.data.repository

import com.example.assetlinkandroid.data.model.NewOffer
import com.example.assetlinkandroid.data.model.Offer
import com.example.assetlinkandroid.data.model.OfferStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfferRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    suspend fun forItem(itemId: String): List<Offer> = supabase.from("offers").select {
        filter { eq("item_id", itemId) }
        order("amount", Order.DESCENDING)
    }.decodeList()

    suspend fun topBid(itemId: String): Offer? = supabase.from("offers").select {
        filter { eq("item_id", itemId) }
        order("amount", Order.DESCENDING)
        limit(1L)
    }.decodeSingleOrNull()

    suspend fun place(newOffer: NewOffer): Offer = supabase.from("offers").insert(newOffer) {
        select()
    }.decodeSingle()

    suspend fun accept(offerId: String, itemId: String) {
        // Mark this offer accepted, all others on this item rejected (within RLS scope).
        supabase.from("offers").update({ set("status", "rejected") }) {
            filter {
                eq("item_id", itemId)
                neq("id", offerId)
            }
        }
        supabase.from("offers").update({ set("status", "accepted") }) {
            filter { eq("id", offerId) }
        }
    }

    suspend fun setStatus(offerId: String, status: OfferStatus) {
        supabase.from("offers").update({ set("status", status.name.lowercase()) }) {
            filter { eq("id", offerId) }
        }
    }

    fun streamForItem(itemId: String): Flow<PostgresAction> {
        val channel = supabase.channel("offers-$itemId")
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "offers"
            filter("item_id", FilterOperator.EQ, itemId)
        }.onStart { channel.subscribe() }
            .onCompletion { runCatching { channel.unsubscribe() } }
    }
}
