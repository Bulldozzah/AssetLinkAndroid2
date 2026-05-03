package com.example.assetlinkandroid.data.session

import com.example.assetlinkandroid.data.model.AppRole

data class UserSession(
    val userId: String,
    val email: String?,
    val roles: Set<AppRole>,
) {
    fun has(role: AppRole) = roles.contains(role)
    val isBorrower get() = has(AppRole.BORROWER)
    val isLender get() = has(AppRole.LENDER)
    val isInspector get() = has(AppRole.INSPECTOR)
    val isLoanOfficer get() = has(AppRole.LOAN_OFFICER)
    val isAdmin get() = has(AppRole.ADMIN)
}
