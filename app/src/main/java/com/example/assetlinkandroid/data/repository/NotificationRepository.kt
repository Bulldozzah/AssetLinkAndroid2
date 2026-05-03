package com.example.assetlinkandroid.data.repository

import com.example.assetlinkandroid.data.model.AppNotification
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
class NotificationRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    suspend fun forUser(uid: String, limit: Int = 50): List<AppNotification> =
        supabase.from("notifications").select {
            filter { eq("user_id", uid) }
            order("created_at", Order.DESCENDING)
            limit(limit.toLong())
        }.decodeList()

    suspend fun unreadCount(uid: String): Int = supabase.from("notifications").select {
        filter {
            eq("user_id", uid)
            eq("read", false)
        }
    }.decodeList<AppNotification>().size

    suspend fun markRead(id: String) {
        supabase.from("notifications").update({ set("read", true) }) {
            filter { eq("id", id) }
        }
    }

    fun streamForUser(uid: String): Flow<PostgresAction> {
        val channel = supabase.channel("notifications-$uid")
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "notifications"
            filter("user_id", FilterOperator.EQ, uid)
        }.onStart { channel.subscribe() }
            .onCompletion { runCatching { channel.unsubscribe() } }
    }
}
