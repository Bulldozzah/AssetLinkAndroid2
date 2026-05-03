package com.example.assetlinkandroid.ui.myitems

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assetlinkandroid.data.model.Item
import com.example.assetlinkandroid.data.repository.AuthRepository
import com.example.assetlinkandroid.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyItemsUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val items: List<Item> = emptyList(),
)

@HiltViewModel
class MyItemsViewModel @Inject constructor(
    private val itemRepo: ItemRepository,
    private val authRepo: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MyItemsUiState())
    val state: StateFlow<MyItemsUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        val uid = authRepo.currentUserId() ?: run {
            _state.value = MyItemsUiState(loading = false)
            return
        }
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching { itemRepo.byBorrower(uid) }
                .onSuccess { _state.value = MyItemsUiState(loading = false, items = it) }
                .onFailure { _state.value = MyItemsUiState(loading = false, error = it.message) }
        }
    }
}
