package com.example.assetlinkandroid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assetlinkandroid.data.repository.AuthRepository
import com.example.assetlinkandroid.data.repository.NotificationRepository
import com.example.assetlinkandroid.data.repository.RoleRepository
import com.example.assetlinkandroid.data.session.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val roleRepo: RoleRepository,
    private val notifRepo: NotificationRepository,
) : ViewModel() {

    private val _session = MutableStateFlow<UserSession?>(null)
    val session: StateFlow<UserSession?> = _session.asStateFlow()

    private val _unreadNotifications = MutableStateFlow(0)
    val unreadNotifications: StateFlow<Int> = _unreadNotifications.asStateFlow()

    private val _pendingPasswordReset = MutableStateFlow(false)
    val pendingPasswordReset: StateFlow<Boolean> = _pendingPasswordReset.asStateFlow()

    fun flagPasswordReset() { _pendingPasswordReset.value = true }
    fun clearPasswordReset() { _pendingPasswordReset.value = false }

    val sessionStatus: StateFlow<SessionStatus> = authRepo.sessionStatus
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionStatus.Initializing)

    init {
        viewModelScope.launch {
            sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        refreshSession()
                        refreshUnreadNotifications()
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _session.value = null
                        _unreadNotifications.value = 0
                    }
                    else -> Unit
                }
            }
        }
    }

    fun refreshSession() {
        val uid = authRepo.currentUserId() ?: run {
            _session.value = null
            return
        }
        viewModelScope.launch {
            val roles = runCatching { roleRepo.rolesFor(uid) }.getOrDefault(emptySet())
            _session.value = UserSession(
                userId = uid,
                email = authRepo.currentEmail(),
                roles = roles,
            )
        }
    }

    fun refreshUnreadNotifications() {
        val uid = authRepo.currentUserId() ?: return
        viewModelScope.launch {
            val n = runCatching { notifRepo.unreadCount(uid) }.getOrDefault(0)
            _unreadNotifications.value = n
        }
    }

    fun getAuthRepo(): AuthRepository = authRepo

    fun signOut() {
        viewModelScope.launch {
            runCatching { authRepo.signOut() }
            _session.value = null
            _unreadNotifications.value = 0
            _pendingPasswordReset.value = false
        }
    }
}
