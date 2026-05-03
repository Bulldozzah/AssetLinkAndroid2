package com.example.assetlinkandroid.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assetlinkandroid.data.model.AppRole
import com.example.assetlinkandroid.data.repository.AuthRepository
import com.example.assetlinkandroid.data.repository.ItemRepository
import com.example.assetlinkandroid.data.repository.LoanRepository
import com.example.assetlinkandroid.data.repository.NotificationRepository
import com.example.assetlinkandroid.data.repository.RoleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val myItemsCount: Int = 0,
    val myLoansCount: Int = 0,
    val unreadCount: Int = 0,
    val statsLoading: Boolean = true,
    val assigningRole: Boolean = false,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val itemRepo: ItemRepository,
    private val loanRepo: LoanRepository,
    private val notifRepo: NotificationRepository,
    private val roleRepo: RoleRepository,
    private val authRepo: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init { refreshStats() }

    fun refresh() { refreshStats() }

    fun refreshStats() {
        val uid = authRepo.currentUserId() ?: return
        _state.value = _state.value.copy(statsLoading = true)
        viewModelScope.launch {
            val itemCount = async {
                runCatching { itemRepo.byBorrower(uid).size }.getOrDefault(0)
            }
            val loanCount = async {
                runCatching {
                    loanRepo.forBorrower(uid).size + loanRepo.forLender(uid).size
                }.getOrDefault(0)
            }
            val unread = async {
                runCatching { notifRepo.unreadCount(uid) }.getOrDefault(0)
            }
            _state.value = _state.value.copy(
                statsLoading = false,
                myItemsCount = itemCount.await(),
                myLoansCount = loanCount.await(),
                unreadCount = unread.await(),
            )
        }
    }

    fun assignRole(role: AppRole, onRefresh: () -> Unit) {
        val uid = authRepo.currentUserId() ?: return
        _state.value = _state.value.copy(assigningRole = true)
        viewModelScope.launch {
            runCatching { roleRepo.assignRole(uid, role) }
            _state.value = _state.value.copy(assigningRole = false)
            onRefresh()
            refreshStats()
        }
    }
}
