package com.example.assetlinkandroid.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assetlinkandroid.data.model.AppNotification
import com.example.assetlinkandroid.data.repository.AuthRepository
import com.example.assetlinkandroid.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val items: List<AppNotification> = emptyList(),
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notifRepo: NotificationRepository,
    private val authRepo: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    init {
        refresh()
        subscribe()
    }

    fun refresh() {
        val uid = authRepo.currentUserId() ?: run {
            _state.value = NotificationsUiState(loading = false)
            return
        }
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching { notifRepo.forUser(uid) }
                .onSuccess { list ->
                    _state.value = NotificationsUiState(loading = false, items = list)
                    if (list.any { !it.read }) {
                        runCatching { notifRepo.markAllRead(uid) }
                    }
                }
                .onFailure { _state.value = NotificationsUiState(loading = false, error = it.message) }
        }
    }

    private fun subscribe() {
        val uid = authRepo.currentUserId() ?: return
        viewModelScope.launch {
            runCatching { notifRepo.streamForUser(uid).collect { _ -> refresh() } }
        }
    }

    fun markRead(id: String) {
        viewModelScope.launch {
            runCatching { notifRepo.markRead(id) }
            refresh()
        }
    }
}
