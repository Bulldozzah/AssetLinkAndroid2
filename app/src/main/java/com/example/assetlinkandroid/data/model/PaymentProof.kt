package com.example.assetlinkandroid.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentProof(
    val id: String,
    @SerialName("loan_id") val loanId: String,
    val kind: String,
    @SerialName("submitter_id") val submitterId: String,
    val amount: Double,
    @SerialName("proof_url") val proofUrl: String,
    val note: String? = null,
    val status: String = "pending",
    @SerialName("reviewed_by") val reviewedBy: String? = null,
    @SerialName("reviewed_at") val reviewedAt: String? = null,
    @SerialName("reviewer_note") val reviewerNote: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class NewPaymentProof(
    @SerialName("loan_id") val loanId: String,
    val kind: String,
    @SerialName("submitter_id") val submitterId: String,
    val amount: Double,
    @SerialName("proof_url") val proofUrl: String,
    val note: String? = null,
)
