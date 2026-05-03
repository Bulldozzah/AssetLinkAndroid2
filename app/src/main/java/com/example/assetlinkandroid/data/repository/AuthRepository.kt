package com.example.assetlinkandroid.data.repository

import com.example.assetlinkandroid.data.model.AppRole
import com.example.assetlinkandroid.data.model.UserRoleRow
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    val sessionStatus: Flow<SessionStatus> = supabase.auth.sessionStatus

    fun currentUserId(): String? = supabase.auth.currentUserOrNull()?.id
    fun currentEmail(): String? = supabase.auth.currentUserOrNull()?.email

    suspend fun signIn(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signUp(email: String, password: String, fullName: String, phone: String?) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("full_name", fullName)
                phone?.let { put("phone", it) }
            }
        }
        // After signup, self-assign default borrower + lender roles per platform default.
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        runCatching {
            supabase.from("user_roles").insert(
                listOf(
                    UserRoleRow(userId = uid, role = AppRole.BORROWER),
                    UserRoleRow(userId = uid, role = AppRole.LENDER),
                )
            )
        }
    }

    suspend fun signOut() {
        supabase.auth.signOut()
    }
}
