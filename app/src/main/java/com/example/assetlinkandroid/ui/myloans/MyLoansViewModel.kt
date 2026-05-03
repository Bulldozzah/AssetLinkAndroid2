package com.example.assetlinkandroid.ui.myloans

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assetlinkandroid.data.model.Item
import com.example.assetlinkandroid.data.model.Loan
import com.example.assetlinkandroid.data.model.NewAppNotification
import com.example.assetlinkandroid.data.model.NewPaymentProof
import com.example.assetlinkandroid.data.model.PaymentProof
import com.example.assetlinkandroid.data.model.ProofKind
import com.example.assetlinkandroid.data.repository.AuthRepository
import com.example.assetlinkandroid.data.repository.ItemRepository
import com.example.assetlinkandroid.data.repository.LoanRepository
import com.example.assetlinkandroid.data.repository.NotificationRepository
import com.example.assetlinkandroid.data.repository.PaymentProofRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class LoanWithItem(
    val loan: Loan,
    val item: Item?,
    val proofs: List<PaymentProof>,
)

data class MyLoansUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val borrowed: List<LoanWithItem> = emptyList(),
    val lent: List<LoanWithItem> = emptyList(),
    val uploadingFor: String? = null,
    val message: String? = null,
)

@HiltViewModel
class MyLoansViewModel @Inject constructor(
    private val loanRepo: LoanRepository,
    private val itemRepo: ItemRepository,
    private val proofRepo: PaymentProofRepository,
    private val notifRepo: NotificationRepository,
    private val authRepo: AuthRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(MyLoansUiState())
    val state: StateFlow<MyLoansUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        val uid = authRepo.currentUserId() ?: run {
            _state.value = MyLoansUiState(loading = false)
            return
        }
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching {
                val borrowed = loanRepo.forBorrower(uid)
                val lent = loanRepo.forLender(uid)
                suspend fun hydrate(loans: List<Loan>): List<LoanWithItem> = loans.map { l ->
                    LoanWithItem(
                        loan = l,
                        item = runCatching { itemRepo.byId(l.itemId) }.getOrNull(),
                        proofs = runCatching { proofRepo.forLoan(l.id) }.getOrDefault(emptyList()),
                    )
                }
                hydrate(borrowed) to hydrate(lent)
            }.onSuccess { (b, l) ->
                _state.value = MyLoansUiState(loading = false, borrowed = b, lent = l)
            }.onFailure { _state.value = MyLoansUiState(loading = false, error = it.message) }
        }
    }

    fun uploadProof(
        loanId: String,
        kind: String,
        amount: Double,
        uri: android.net.Uri,
        note: String? = null,
    ) {
        val uid = authRepo.currentUserId() ?: return
        _state.value = _state.value.copy(uploadingFor = loanId, message = null)
        viewModelScope.launch {
            val r = runCatching {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("Couldn't read selected image.")
                val ext = (context.contentResolver.getType(uri) ?: "image/jpeg")
                    .substringAfter('/').substringBefore(';').ifBlank { "jpg" }
                val path = "$uid/${loanId}-${kind}-${UUID.randomUUID()}.$ext"
                val publicUrl = proofRepo.upload(path, bytes)
                proofRepo.submit(
                    NewPaymentProof(
                        loanId = loanId,
                        kind = kind,
                        submitterId = uid,
                        amount = amount,
                        proofUrl = publicUrl,
                        note = note?.ifBlank { null },
                    )
                )
                // Notify counterparty
                val loan = loanRepo.byId(loanId)
                if (loan != null) {
                    val recipientId = if (kind == FUNDING) loan.borrowerId else loan.lenderId
                    val title = if (kind == FUNDING)
                        "Funding proof submitted — awaiting loan officer review"
                    else
                        "Repayment proof submitted — awaiting loan officer review"
                    runCatching {
                        notifRepo.notify(NewAppNotification(userId = recipientId, title = title))
                    }
                }
            }
            _state.value = _state.value.copy(
                uploadingFor = null,
                message = r.exceptionOrNull()?.message
                    ?: "Proof submitted. Awaiting officer review.",
            )
            refresh()
        }
    }

    fun clearMessage() { _state.value = _state.value.copy(message = null) }

    companion object {
        const val FUNDING = ProofKind.FUNDING
        const val REPAYMENT = ProofKind.REPAYMENT
    }
}
