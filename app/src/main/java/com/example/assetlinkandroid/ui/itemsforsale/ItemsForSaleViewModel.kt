package com.example.assetlinkandroid.ui.itemsforsale

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assetlinkandroid.data.model.NewAppNotification
import com.example.assetlinkandroid.data.model.SaleListing
import com.example.assetlinkandroid.data.model.toSaleListing
import com.example.assetlinkandroid.data.repository.AuthRepository
import com.example.assetlinkandroid.data.repository.ItemSaleRepository
import com.example.assetlinkandroid.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil

data class ItemsForSaleUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val allSales: List<SaleListing> = emptyList(),
    val query: String = "",
    val minPrice: String = "",
    val maxPrice: String = "",
    val busy: Boolean = false,
    val message: String? = null,
) {
    val filtered: List<SaleListing>
        get() {
            val text = query.trim().lowercase()
            val minP = minPrice.toDoubleOrNull()
            val maxP = maxPrice.toDoubleOrNull()
            return allSales.filter { s ->
                if (text.isNotEmpty()) {
                    val hay = "${s.itemTitle} ${s.itemDescription ?: ""}".lowercase()
                    if (!hay.contains(text)) return@filter false
                }
                if (minP != null && s.salePrice < minP) return@filter false
                if (maxP != null && s.salePrice > maxP) return@filter false
                true
            }
        }

    val hasActiveFilters: Boolean
        get() = query.isNotBlank() || minPrice.isNotBlank() || maxPrice.isNotBlank()
}

@HiltViewModel
class ItemsForSaleViewModel @Inject constructor(
    private val saleRepo: ItemSaleRepository,
    private val notifRepo: NotificationRepository,
    private val authRepo: AuthRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(ItemsForSaleUiState())
    val state: StateFlow<ItemsForSaleUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching { saleRepo.listedSales().map { it.toSaleListing() } }
                .onSuccess { _state.value = _state.value.copy(loading = false, allSales = it) }
                .onFailure { _state.value = _state.value.copy(loading = false, error = it.message) }
        }
    }

    fun updateQuery(q: String) { _state.value = _state.value.copy(query = q) }
    fun updateMinPrice(v: String) { _state.value = _state.value.copy(minPrice = v) }
    fun updateMaxPrice(v: String) { _state.value = _state.value.copy(maxPrice = v) }
    fun clearMessage() { _state.value = _state.value.copy(message = null) }

    /** Buyer accepts a listed sale → under_offer. */
    fun acceptSale(sale: SaleListing) {
        val uid = authRepo.currentUserId() ?: return
        _state.value = _state.value.copy(busy = true, message = null)
        viewModelScope.launch {
            val r = runCatching {
                saleRepo.acceptSale(sale.id, uid)
                saleRepo.markItemUnderOffer(sale.itemId)
                // Notify loan officer who flagged it
                runCatching {
                    notifRepo.notify(
                        NewAppNotification(
                            userId = sale.flaggedBy,
                            title = "Sale accepted — under offer",
                            body = "A buyer accepted \"${sale.itemTitle}\" for \$${ceil(sale.salePrice).toInt()}. Awaiting proof of payment.",
                            link = "/loan-officer",
                        )
                    )
                }
            }
            _state.value = _state.value.copy(
                busy = false,
                message = r.exceptionOrNull()?.message
                    ?: "Item accepted — now upload your proof of payment.",
            )
            refresh()
        }
    }

    /** Upload proof-of-payment image and submit it. */
    fun submitProof(sale: SaleListing, uri: Uri, note: String?) {
        val uid = authRepo.currentUserId() ?: return
        if (sale.buyerId != uid) return
        _state.value = _state.value.copy(busy = true, message = null)
        viewModelScope.launch {
            val r = runCatching {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("Couldn't read selected image.")
                val ext = (context.contentResolver.getType(uri) ?: "image/png")
                    .substringAfter('/').substringBefore(';').ifBlank { "png" }
                val proofUrl = saleRepo.uploadPop(sale.id, bytes, ext)
                saleRepo.submitProof(sale.id, proofUrl, note?.ifBlank { null })
                // Notify the loan officer
                runCatching {
                    notifRepo.notify(
                        NewAppNotification(
                            userId = sale.flaggedBy,
                            title = "Purchase proof submitted",
                            body = "A buyer submitted payment proof for \"${sale.itemTitle}\". Please review and approve.",
                            link = "/loan-officer",
                        )
                    )
                }
            }
            _state.value = _state.value.copy(
                busy = false,
                message = r.exceptionOrNull()?.message
                    ?: "Proof submitted. Awaiting officer review.",
            )
            refresh()
        }
    }
}
