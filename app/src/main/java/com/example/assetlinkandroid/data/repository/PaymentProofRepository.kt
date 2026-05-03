package com.example.assetlinkandroid.data.repository

import com.example.assetlinkandroid.data.model.NewPaymentProof
import com.example.assetlinkandroid.data.model.PaymentProof
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentProofRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    private val bucket get() = supabase.storage.from("payment-proofs")

    suspend fun upload(path: String, bytes: ByteArray): String {
        bucket.upload(path, bytes) { upsert = false }
        return bucket.publicUrl(path)
    }

    suspend fun submit(proof: NewPaymentProof): PaymentProof =
        supabase.from("payment_proofs").insert(proof) { select() }.decodeSingle()

    suspend fun forLoan(loanId: String): List<PaymentProof> = supabase.from("payment_proofs").select {
        filter { eq("loan_id", loanId) }
        order("created_at", Order.DESCENDING)
    }.decodeList()

    suspend fun pending(): List<PaymentProof> = supabase.from("payment_proofs").select {
        filter { eq("status", "pending") }
        order("created_at", Order.ASCENDING)
    }.decodeList()
}
