package com.example.assetlinkandroid.data.repository

import com.example.assetlinkandroid.data.model.Profile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    suspend fun byId(id: String): Profile? {
        return runCatching {
            supabase.from("profiles").select {
                filter { eq("id", id) }
                limit(1L)
            }.decodeSingleOrNull<Profile>()
        }.getOrNull()
    }

    suspend fun byIds(ids: Collection<String>): Map<String, Profile> {
        if (ids.isEmpty()) return emptyMap()
        return runCatching {
            supabase.from("profiles").select {
                filter { isIn("id", ids.toList()) }
            }.decodeList<Profile>().associateBy { it.id }
        }.getOrDefault(emptyMap())
    }
}
