package com.example.assetlinkandroid.ui.itemdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assetlinkandroid.data.model.Item
import com.example.assetlinkandroid.data.model.ItemStatus
import com.example.assetlinkandroid.data.model.LoanStatus
import com.example.assetlinkandroid.data.model.NewLoan
import com.example.assetlinkandroid.data.model.NewOffer
import com.example.assetlinkandroid.data.model.OfferStatus
import kotlin.math.ceil
import kotlin.math.floor
import com.example.assetlinkandroid.data.model.Offer
import com.example.assetlinkandroid.data.model.Profile
import com.example.assetlinkandroid.data.repository.ItemRepository
import com.example.assetlinkandroid.data.repository.LoanRepository
import com.example.assetlinkandroid.data.repository.OfferRepository
import com.example.assetlinkandroid.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ItemDetailUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val item: Item? = null,
    val offers: List<Offer> = emptyList(),
    val lenderProfiles: Map<String, Profile> = emptyMap(),
    val borrowerProfile: Profile? = null,
    val placingBid: Boolean = false,
    val accepting: Boolean = false,
    val selectedOfferId: String? = null,
    val message: String? = null,
) {
    // Highest pending offer visible to this client (may be limited by RLS)
    private val topFromOffers: Double?
        get() = offers.filter { it.status == OfferStatus.PENDING }
            .maxByOrNull { it.amount }?.amount

    // DB-trigger-maintained column — visible to everyone regardless of RLS
    private val topFromItem: Double? get() = item?.currentTopBid

    // True top bid: max of both sources (mirrors web logic)
    val currentTopBid: Double?
        get() = when {
            topFromOffers != null && topFromItem != null -> maxOf(topFromOffers!!, topFromItem!!)
            else -> topFromOffers ?: topFromItem
        }

    // Highest pending offer object (for display / accept flow)
    val topBid: Offer?
        get() = offers.filter { it.status == OfferStatus.PENDING }.maxByOrNull { it.amount }

    // Minimum valid next bid
    val minBidAmount: Int
        get() = currentTopBid
            ?.let { floor(it).toInt() + 1 }
            ?: ceil(item?.reserveAmount ?: 0.0).toInt()

    val selectedOffer: Offer? get() = offers.firstOrNull { it.id == selectedOfferId }
}

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val itemRepo: ItemRepository,
    private val offerRepo: OfferRepository,
    private val loanRepo: LoanRepository,
    private val profileRepo: ProfileRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val itemId: String = savedStateHandle.get<String>("itemId") ?: ""

    private val _state = MutableStateFlow(ItemDetailUiState())
    val state: StateFlow<ItemDetailUiState> = _state.asStateFlow()

    init {
        if (itemId.isNotBlank()) {
            refresh()
            subscribeRealtime()
        }
    }

    fun refresh() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching {
                val item = itemRepo.byId(itemId) ?: error("Item not found")
                val offers = offerRepo.forItem(itemId)
                val lenderIds = offers.map { it.lenderId }.toSet()
                val lenderProfiles = profileRepo.byIds(lenderIds)
                val borrower = profileRepo.byId(item.borrowerId)
                Triple(item, offers, Pair(lenderProfiles, borrower))
            }.onSuccess { (item, offers, profiles) ->
                _state.value = ItemDetailUiState(
                    loading = false,
                    item = item,
                    offers = offers,
                    lenderProfiles = profiles.first,
                    borrowerProfile = profiles.second,
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(loading = false, error = e.message)
            }
        }
    }

    private fun subscribeRealtime() {
        viewModelScope.launch {
            runCatching {
                offerRepo.streamForItem(itemId).collect { _ -> refresh() }
            }
        }
        viewModelScope.launch {
            runCatching {
                itemRepo.streamItem(itemId).collect { _ -> refresh() }
            }
        }
    }

    fun placeBid(lenderId: String, amount: Int, onDone: () -> Unit) {
        val s = _state.value
        val item = s.item ?: return
        val min = s.minBidAmount
        if (amount < min) {
            val msg = if (s.currentTopBid != null)
                "Bid must be higher than the current \$${floor(s.currentTopBid!!).toInt()}"
            else
                "Bid must meet the reserve of \$${ceil(item.reserveAmount).toInt()}"
            _state.value = s.copy(message = msg)
            return
        }
        _state.value = s.copy(placingBid = true, message = null)
        viewModelScope.launch {
            val r = runCatching {
                offerRepo.place(
                    NewOffer(
                        itemId = item.id,
                        lenderId = lenderId,
                        amount = amount.toDouble(),
                        proposedInterest = item.interestRate,
                    )
                )
                // Mark all earlier offers on this item as withdrawn (superseded)
                runCatching {
                    s.offers.filter { it.lenderId != lenderId && it.status == OfferStatus.PENDING }
                        .forEach { o ->
                            offerRepo.setStatus(o.id, OfferStatus.WITHDRAWN)
                        }
                }
            }
            _state.value = _state.value.copy(
                placingBid = false,
                message = r.exceptionOrNull()?.message ?: "Bid placed.",
            )
            onDone()
            refresh()
        }
    }

    fun selectOffer(offerId: String?) {
        _state.value = _state.value.copy(selectedOfferId = offerId)
    }

    fun acceptOffer(offerId: String, onDone: () -> Unit) {
        val s = _state.value
        val item = s.item ?: return
        val chosen = s.offers.firstOrNull { it.id == offerId } ?: return
        _state.value = s.copy(accepting = true, message = null)
        viewModelScope.launch {
            val r = runCatching {
                offerRepo.accept(chosen.id, item.id)
                val totalRepayment = chosen.amount * (1.0 + item.interestRate)
                loanRepo.create(
                    NewLoan(
                        itemId = item.id,
                        borrowerId = item.borrowerId,
                        lenderId = chosen.lenderId,
                        principal = chosen.amount,
                        interestRate = item.interestRate,
                        durationDays = item.loanDurationDays,
                        totalRepayment = totalRepayment,
                        platformCommission = totalRepayment - chosen.amount,
                    )
                )
                itemRepo.setStatus(item.id, ItemStatus.BID_CLOSED)
            }
            _state.value = _state.value.copy(
                accepting = false,
                selectedOfferId = if (r.isSuccess) null else _state.value.selectedOfferId,
                message = r.exceptionOrNull()?.message ?: "Bid accepted.",
            )
            onDone()
            refresh()
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
