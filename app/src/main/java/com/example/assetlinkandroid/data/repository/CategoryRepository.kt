package com.example.assetlinkandroid.data.repository

import com.example.assetlinkandroid.data.model.ItemCategory
import com.example.assetlinkandroid.data.model.ItemSubcategory
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    suspend fun categories(): List<ItemCategory> = runCatching {
        supabase.from("item_categories").select {
            order("sort_order", Order.ASCENDING)
        }.decodeList<ItemCategory>()
    }.getOrDefault(emptyList())

    suspend fun subcategories(categoryId: String): List<ItemSubcategory> = runCatching {
        supabase.from("item_subcategories").select {
            filter { eq("category_id", categoryId) }
            order("sort_order", Order.ASCENDING)
        }.decodeList<ItemSubcategory>()
    }.getOrDefault(emptyList())
}
