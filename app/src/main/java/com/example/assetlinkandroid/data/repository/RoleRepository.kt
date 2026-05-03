package com.example.assetlinkandroid.data.repository

import com.example.assetlinkandroid.data.model.AppRole
import com.example.assetlinkandroid.data.model.UserRoleRow
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoleRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    suspend fun rolesFor(userId: String): Set<AppRole> {
        val rows = supabase.from("user_roles").select {
            filter { eq("user_id", userId) }
        }.decodeList<UserRoleRow>()
        return rows.map { it.role }.toSet()
    }

    suspend fun assignRole(userId: String, role: AppRole) {
        supabase.from("user_roles").upsert(
            UserRoleRow(userId = userId, role = role)
        )
    }

    suspend fun selfAssignBorrowerLender(userId: String) {
        runCatching {
            supabase.from("user_roles").insert(
                listOf(
                    UserRoleRow(userId = userId, role = AppRole.BORROWER),
                    UserRoleRow(userId = userId, role = AppRole.LENDER),
                )
            )
        }
    }
}
