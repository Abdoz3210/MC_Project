package com.lafamilia.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lafamilia.app.AppGraph
import com.lafamilia.app.data.auth.AuthRepository
import com.lafamilia.app.domain.AuthValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository = AppGraph.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = edit { it.copy(name = value) }
    fun onEmailChange(value: String) = edit { it.copy(email = value) }
    fun onPasswordChange(value: String) = edit { it.copy(password = value) }
    fun onConfirmPasswordChange(value: String) = edit { it.copy(confirmPassword = value) }

    fun register() {
        val current = _uiState.value
        if (current.isLoading) return

        val problem = AuthValidator.validateRegister(
            name = current.name,
            email = current.email,
            password = current.password,
            confirmPassword = current.confirmPassword
        )
        if (problem != null) {
            _uiState.update { it.copy(validationError = problem) }
            return
        }

        _uiState.update { it.copy(isLoading = true, validationError = null, serverError = null) }
        viewModelScope.launch {
            authRepository.register(current.name.trim(), current.email.trim(), current.password)
                .onSuccess { _uiState.update { it.copy(isLoading = false, isRegistered = true) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, serverError = error.localizedMessage.orEmpty()) }
                }
        }
    }

    /** Applies [change] and clears any old error, because the user is fixing the form. */
    private fun edit(change: (RegisterUiState) -> RegisterUiState) {
        _uiState.update { change(it).copy(validationError = null, serverError = null) }
    }
}
