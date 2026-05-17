package com.example.assetlinkandroid.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assetlinkandroid.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val mode: Mode = Mode.SIGN_IN,
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val phone: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val registrationSuccess: Boolean = false,
    val showForgotPassword: Boolean = false,
    val forgotPasswordEmail: String = "",
    val forgotPasswordLoading: Boolean = false,
    val forgotPasswordSuccess: Boolean = false,
    val forgotPasswordError: String? = null,
) {
    enum class Mode { SIGN_IN, SIGN_UP }
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun setMode(mode: AuthUiState.Mode) = update { it.copy(mode = mode, error = null, registrationSuccess = false) }
    fun dismissRegistrationSuccess() = update { it.copy(registrationSuccess = false, mode = AuthUiState.Mode.SIGN_IN) }

    fun showForgotPassword() = update {
        it.copy(showForgotPassword = true, forgotPasswordEmail = it.email, forgotPasswordError = null, forgotPasswordSuccess = false)
    }
    fun dismissForgotPassword() = update {
        it.copy(showForgotPassword = false, forgotPasswordEmail = "", forgotPasswordError = null, forgotPasswordSuccess = false)
    }
    fun setForgotPasswordEmail(v: String) = update { it.copy(forgotPasswordEmail = v.trim(), forgotPasswordError = null) }

    fun sendPasswordReset() {
        val email = _state.value.forgotPasswordEmail
        if (email.isBlank() || !email.contains("@")) {
            update { it.copy(forgotPasswordError = "Please enter a valid email address.") }
            return
        }
        update { it.copy(forgotPasswordLoading = true, forgotPasswordError = null) }
        viewModelScope.launch {
            val result = runCatching { authRepo.resetPasswordForEmail(email) }
            update {
                if (result.isSuccess) {
                    it.copy(forgotPasswordLoading = false, forgotPasswordSuccess = true)
                } else {
                    val raw = result.exceptionOrNull()?.message?.lowercase().orEmpty()
                    val friendly = when {
                        "rate limit" in raw || "too many requests" in raw -> "Too many attempts. Please try again later."
                        "network" in raw || "timeout" in raw || "unable to resolve host" in raw -> "Network error. Check your connection."
                        "user not found" in raw -> "No account found with this email."
                        else -> "Unable to send reset link. Please check your email and try again."
                    }
                    it.copy(forgotPasswordLoading = false, forgotPasswordError = friendly)
                }
            }
        }
    }
    fun setEmail(v: String) = update { it.copy(email = v.trim(), error = null) }
    fun setPassword(v: String) = update { it.copy(password = v, error = null) }
    fun setFullName(v: String) = update { it.copy(fullName = v, error = null) }
    fun setPhone(v: String) = update { it.copy(phone = v.trim(), error = null) }

    fun submit(onSuccess: () -> Unit) {
        val s = _state.value
        if (s.email.isBlank() || s.password.length < 6) {
            update { it.copy(error = "Enter a valid email and password (min 6 chars).") }
            return
        }
        if (s.mode == AuthUiState.Mode.SIGN_UP && s.fullName.isBlank()) {
            update { it.copy(error = "Full name is required.") }
            return
        }
        update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val result = runCatching {
                if (s.mode == AuthUiState.Mode.SIGN_IN) {
                    authRepo.signIn(s.email, s.password)
                } else {
                    authRepo.signUp(s.email, s.password, s.fullName.trim(), s.phone.ifBlank { null })
                }
            }
            if (result.isSuccess && s.mode == AuthUiState.Mode.SIGN_UP) {
                update { it.copy(loading = false, error = null, registrationSuccess = true) }
            } else {
                update { it.copy(loading = false, error = friendlyAuthError(result.exceptionOrNull())) }
                if (result.isSuccess) onSuccess()
            }
        }
    }

    private fun friendlyAuthError(e: Throwable?): String? {
        val msg = e?.message?.lowercase() ?: return null
        return when {
            "invalid login credentials" in msg ||
            "invalid_credentials" in msg      -> "Incorrect email or password."
            "email not confirmed" in msg       -> "Please verify your email before signing in."
            "user already registered" in msg   -> "An account with this email already exists."
            "rate limit" in msg ||
            "too many requests" in msg         -> "Too many attempts. Please try again later."
            "network" in msg ||
            "timeout" in msg ||
            "unable to resolve host" in msg    -> "Network error. Check your connection."
            "user not found" in msg            -> "No account found with this email."
            "weak password" in msg             -> "Password is too weak. Use at least 6 characters."
            else                               -> "Something went wrong. Please try again."
        }
    }

    private inline fun update(block: (AuthUiState) -> AuthUiState) {
        _state.value = block(_state.value)
    }
}
