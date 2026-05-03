package com.example.assetlinkandroid.ui.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assetlinkandroid.data.model.Item
import com.example.assetlinkandroid.data.model.ItemCategory
import com.example.assetlinkandroid.data.model.ItemSubcategory
import com.example.assetlinkandroid.data.repository.CategoryRepository
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

data class BrowseUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val allItems: List<Item> = emptyList(),
    val categories: List<ItemCategory> = emptyList(),
    val allSubcategories: List<ItemSubcategory> = emptyList(),
    val topBids: Map<String, Double?> = emptyMap(),
    val searchQuery: String = "",
    val selectedCategoryId: String? = null,
    val selectedSubcategoryId: String? = null,
    val minPrice: String = "",
    val maxPrice: String = "",
    val minInterest: String = "",
    val maxInterest: String = "",
) {
    val visibleSubcategories: List<ItemSubcategory>
        get() = selectedCategoryId?.let { catId ->
            allSubcategories.filter { it.categoryId == catId }
        } ?: emptyList()

    val filteredItems: List<Item>
        get() {
            var result = allItems

            if (searchQuery.isNotBlank()) {
                val q = searchQuery.lowercase()
                result = result.filter {
                    it.title.lowercase().contains(q) ||
                    it.description?.lowercase()?.contains(q) == true
                }
            }

            selectedCategoryId?.let { catId ->
                val catName = categories.find { it.id == catId }?.name?.lowercase()
                if (catName != null) {
                    result = result.filter {
                        it.itemType?.lowercase()?.contains(catName) == true
                    }
                }
            }

            selectedSubcategoryId?.let { subId ->
                val subName = allSubcategories.find { it.id == subId }?.name?.lowercase()
                if (subName != null) {
                    result = result.filter {
                        it.itemType?.lowercase()?.contains(subName) == true
                    }
                }
            }

            minPrice.toDoubleOrNull()?.let { min ->
                result = result.filter { it.reserveAmount >= min }
            }
            maxPrice.toDoubleOrNull()?.let { max ->
                result = result.filter { it.reserveAmount <= max }
            }
            minInterest.toDoubleOrNull()?.let { min ->
                result = result.filter { it.interestRate * 100 >= min }
            }
            maxInterest.toDoubleOrNull()?.let { max ->
                result = result.filter { it.interestRate * 100 <= max }
            }

            return result
        }

    val hasActiveFilters: Boolean
        get() = searchQuery.isNotBlank() || selectedCategoryId != null ||
                selectedSubcategoryId != null || minPrice.isNotBlank() ||
                maxPrice.isNotBlank() || minInterest.isNotBlank() || maxInterest.isNotBlank()
}

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val itemRepo: ItemRepository,
    private val offerRepo: OfferRepository,
    private val categoryRepo: CategoryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BrowseUiState())
    val state: StateFlow<BrowseUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val itemsD = async { runCatching { itemRepo.listedCatalogue() }.getOrDefault(emptyList()) }
            val catsD  = async { runCatching { categoryRepo.categories() }.getOrDefault(emptyList()) }
            val subsD  = async { runCatching { categoryRepo.allSubcategories() }.getOrDefault(emptyList()) }

            val items = itemsD.await()
            val cats  = catsD.await()
            val subs  = subsD.await()

            val tops = items.map { item ->
                async { item.id to runCatching { offerRepo.topBid(item.id)?.amount }.getOrNull() }
            }.awaitAll().toMap()

            _state.value = _state.value.copy(
                loading = false,
                allItems = items,
                categories = cats,
                allSubcategories = subs,
                topBids = tops,
            )
        }
    }

    fun setSearch(q: String)       = update { it.copy(searchQuery = q) }
    fun setCategory(id: String?)   = update { it.copy(selectedCategoryId = id, selectedSubcategoryId = null) }
    fun setSubcategory(id: String?) = update { it.copy(selectedSubcategoryId = id) }
    fun setMinPrice(v: String)     = update { it.copy(minPrice = v) }
    fun setMaxPrice(v: String)     = update { it.copy(maxPrice = v) }
    fun setMinInterest(v: String)  = update { it.copy(minInterest = v) }
    fun setMaxInterest(v: String)  = update { it.copy(maxInterest = v) }
    fun resetFilters()             = update {
        it.copy(
            searchQuery = "", selectedCategoryId = null, selectedSubcategoryId = null,
            minPrice = "", maxPrice = "", minInterest = "", maxInterest = "",
        )
    }

    private inline fun update(block: (BrowseUiState) -> BrowseUiState) {
        _state.value = block(_state.value)
    }
}
