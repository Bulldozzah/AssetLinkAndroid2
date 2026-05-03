package com.example.assetlinkandroid.data.repository

import com.example.assetlinkandroid.data.model.Loan
import com.example.assetlinkandroid.data.model.LoanStatus
import com.example.assetlinkandroid.data.model.NewLoan
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoanRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    suspend fun byId(id: String): Loan? = supabase.from("loans").select {
        filter { eq("id", id) }
        limit(1L)
    }.decodeSingleOrNull()

    suspend fun forBorrower(uid: String): List<Loan> = supabase.from("loans").select {
        filter { eq("borrower_id", uid) }
        order("created_at", Order.DESCENDING)
    }.decodeList()

    suspend fun forLender(uid: String): List<Loan> = supabase.from("loans").select {
        filter { eq("lender_id", uid) }
        order("created_at", Order.DESCENDING)
    }.decodeList()

    suspend fun forItem(itemId: String): Loan? = supabase.from("loans").select {
        filter { eq("item_id", itemId) }
        order("created_at", Order.DESCENDING)
        limit(1L)
    }.decodeSingleOrNull()

    suspend fun create(loan: NewLoan): Loan = supabase.from("loans").insert(loan) {
        select()
    }.decodeSingle()

    suspend fun setStatus(loanId: String, status: LoanStatus) {
        supabase.from("loans").update({ set("status", status.name.lowercase()) }) {
            filter { eq("id", loanId) }
        }
    }
}
