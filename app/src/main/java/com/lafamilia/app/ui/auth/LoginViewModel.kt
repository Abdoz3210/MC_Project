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

class LoginViewModel(
    private val authRepository: AuthRepository = AppGraph.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, validationError = null, serverError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, validationError = null, serverError = null) }
    }

    fun login() {
        val current = _uiState.value
        if (current.isLoading) return

        val problem = AuthValidator.validateLogin(current.email, current.password)
        if (problem != null) {
            _uiState.update { it.copy(validationError = problem) }
            return
        }

        _uiState.update { it.copy(isLoading = true, validationError = null, serverError = null) }
        viewModelScope.launch {
            authRepository.login(current.email.trim(), current.password)
                .onSuccess { _uiState.update { it.copy(isLoading = false, isLoggedIn = true) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, serverError = error.localizedMessage.orEmpty()) }
                }
        }
    }
}
