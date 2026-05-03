package com.example.assetlinkandroid.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assetlinkandroid.data.model.Item
import com.example.assetlinkandroid.data.repository.ItemRepository
import com.example.assetlinkandroid.data.repository.OfferRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val items: List<Item> = emptyList(),
    val topBids: Map<String, Double?> = emptyMap(),
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val itemRepo: ItemRepository,
    private val offerRepo: OfferRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val result = runCatching { itemRepo.listedCatalogue() }
            result.onSuccess { items ->
                val tops = runCatching {
                    items.map { item ->
                        async { item.id to (offerRepo.topBid(item.id)?.amount) }
                    }.awaitAll().toMap()
                }.getOrDefault(emptyMap())
                _state.value = DashboardUiState(loading = false, items = items, topBids = tops)
            }.onFailure { err ->
                _state.value = _state.value.copy(loading = false, error = err.message)
            }
        }
    }
}
