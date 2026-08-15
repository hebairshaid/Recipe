package com.recipe.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipe.auth.domain.model.PasswordPolicy
import com.recipe.auth.domain.model.User
import com.recipe.auth.domain.repository.AuthRepository
import com.recipe.auth.domain.session.SessionRepository
import com.recipe.auth.domain.session.SessionToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val isEditingPassword: Boolean = false,
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isUpdatingPassword: Boolean = false,
    val message: String? = null,
    val isErrorMessage: Boolean = false,
    val loggedOut: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val email = SessionToken.emailFrom(sessionRepository.getToken())
            if (email.isBlank()) {
                _uiState.update {
                    it.copy(isLoading = false, loggedOut = true)
                }
                return@launch
            }

            val user = authRepository.getUserByEmail(email)
            if (user == null) {
                sessionRepository.clearToken()
                _uiState.update { it.copy(isLoading = false, loggedOut = true) }
            } else {
                _uiState.update { it.copy(user = user, isLoading = false) }
            }
        }
    }

    fun onEditPasswordClick() {
        _uiState.update {
            it.copy(
                isEditingPassword = true,
                message = null,
                currentPassword = "",
                newPassword = "",
                confirmPassword = "",
                currentPasswordError = null,
                newPasswordError = null,
                confirmPasswordError = null,
            )
        }
    }

    fun onCancelEditPassword() {
        _uiState.update {
            it.copy(
                isEditingPassword = false,
                currentPassword = "",
                newPassword = "",
                confirmPassword = "",
                currentPasswordError = null,
                newPasswordError = null,
                confirmPasswordError = null,
                message = null,
            )
        }
    }

    fun onCurrentPasswordChange(value: String) {
        _uiState.update {
            it.copy(currentPassword = value, currentPasswordError = null, message = null)
        }
    }

    fun onNewPasswordChange(value: String) {
        _uiState.update {
            it.copy(newPassword = value, newPasswordError = null, message = null)
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update {
            it.copy(confirmPassword = value, confirmPasswordError = null, message = null)
        }
    }

    fun updatePassword() {
        val state = _uiState.value
        val email = state.user?.email ?: return

        var currentError: String? = null
        var newError: String? = null
        var confirmError: String? = null

        if (state.currentPassword.isBlank()) {
            currentError = "Current password is required"
        }

        try {
            PasswordPolicy.validate(state.newPassword)
        } catch (e: IllegalArgumentException) {
            newError = e.message
        }

        if (state.newPassword != state.confirmPassword) {
            confirmError = "Passwords do not match"
        }

        if (currentError != null || newError != null || confirmError != null) {
            _uiState.update {
                it.copy(
                    currentPasswordError = currentError,
                    newPasswordError = newError,
                    confirmPasswordError = confirmError,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingPassword = true, message = null) }
            runCatching {
                authRepository.updatePassword(
                    email = email,
                    currentPassword = state.currentPassword,
                    newPassword = state.newPassword,
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isUpdatingPassword = false,
                        isEditingPassword = false,
                        currentPassword = "",
                        newPassword = "",
                        confirmPassword = "",
                        message = "Password updated successfully",
                        isErrorMessage = false,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isUpdatingPassword = false,
                        message = error.message ?: "Failed to update password",
                        isErrorMessage = true,
                    )
                }
            }
        }
    }

    fun logout() {
        sessionRepository.clearToken()
        _uiState.update { it.copy(loggedOut = true) }
    }
}
