package com.example.assetlinkandroid.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AppRole {
    @SerialName("admin") ADMIN,
    @SerialName("inspector") INSPECTOR,
    @SerialName("storage_partner") STORAGE_PARTNER,
    @SerialName("borrower") BORROWER,
    @SerialName("lender") LENDER,
    @SerialName("loan_officer") LOAN_OFFICER;

    val display: String
        get() = name.lowercase().split('_').joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
}

@Serializable
enum class ItemStatus {
    @SerialName("pending") PENDING,
    @SerialName("approved") APPROVED,
    @SerialName("listed") LISTED,
    @SerialName("bid_closed") BID_CLOSED,
    @SerialName("funded") FUNDED,
    @SerialName("repaid") REPAID,
    @SerialName("auctioned") AUCTIONED,
    @SerialName("rejected") REJECTED;
}

@Serializable
enum class OfferStatus {
    @SerialName("pending") PENDING,
    @SerialName("accepted") ACCEPTED,
    @SerialName("rejected") REJECTED,
    @SerialName("withdrawn") WITHDRAWN;
}

@Serializable
enum class LoanStatus {
    @SerialName("pending") PENDING,
    @SerialName("active") ACTIVE,
    @SerialName("repaid") REPAID,
    @SerialName("defaulted") DEFAULTED,
    @SerialName("cancelled") CANCELLED;
}

object ProofKind {
    const val FUNDING = "funding"
    const val REPAYMENT = "repayment"
}

object ProofStatus {
    const val PENDING = "pending"
    const val APPROVED = "approved"
    const val REJECTED = "rejected"
}
