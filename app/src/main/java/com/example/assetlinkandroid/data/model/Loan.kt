package com.example.assetlinkandroid.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Loan(
    val id: String,
    @SerialName("item_id") val itemId: String,
    @SerialName("borrower_id") val borrowerId: String,
    @SerialName("lender_id") val lenderId: String,
    val principal: Double,
    @SerialName("interest_rate") val interestRate: Double,
    @SerialName("duration_days") val durationDays: Int,
    @SerialName("total_repayment") val totalRepayment: Double,
    @SerialName("platform_commission") val platformCommission: Double,
    val status: LoanStatus = LoanStatus.PENDING,
    @SerialName("funded_at") val fundedAt: String? = null,
    @SerialName("due_at") val dueAt: String? = null,
    @SerialName("repaid_at") val repaidAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class NewLoan(
    @SerialName("item_id") val itemId: String,
    @SerialName("borrower_id") val borrowerId: String,
    @SerialName("lender_id") val lenderId: String,
    val principal: Double,
    @SerialName("interest_rate") val interestRate: Double,
    @SerialName("duration_days") val durationDays: Int,
    @SerialName("total_repayment") val totalRepayment: Double,
    @SerialName("platform_commission") val platformCommission: Double,
)
