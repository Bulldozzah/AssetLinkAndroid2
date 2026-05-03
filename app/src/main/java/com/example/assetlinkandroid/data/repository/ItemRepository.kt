package com.example.assetlinkandroid.data.repository

import com.example.assetlinkandroid.data.model.Item
import com.example.assetlinkandroid.data.model.ItemStatus
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
class ItemRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    suspend fun listedCatalogue(limit: Int = 50): List<Item> = supabase.from("items").select {
        filter { isIn("status", listOf("listed", "bid_closed", "funded", "auctioned")) }
        order("created_at", Order.DESCENDING)
        limit(limit.toLong())
    }.decodeList()

    suspend fun byId(id: String): Item? = supabase.from("items").select {
        filter { eq("id", id) }
        limit(1L)
    }.decodeSingleOrNull()

    suspend fun byBorrower(borrowerId: String): List<Item> = supabase.from("items").select {
        filter { eq("borrower_id", borrowerId) }
        order("created_at", Order.DESCENDING)
    }.decodeList()

    suspend fun setStatus(itemId: String, status: ItemStatus) {
        supabase.from("items").update(
            { set("status", status.name.lowercase()) }
        ) {
            filter { eq("id", itemId) }
        }
    }

    /** Realtime stream of changes to a single item row. */
    fun streamItem(itemId: String): Flow<PostgresAction> {
        val channel = supabase.channel("item-$itemId")
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "items"
            filter("id", FilterOperator.EQ, itemId)
        }.onStart { channel.subscribe() }
            .onCompletion { runCatching { channel.unsubscribe() } }
    }
}
