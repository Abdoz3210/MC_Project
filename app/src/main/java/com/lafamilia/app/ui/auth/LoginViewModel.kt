
package com.lafamilia.app.ui.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _uiState.update { it.copy(email = newEmail, error = null) }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(password = newPassword, error = null) }
    }

    private val auth = FirebaseAuth.getInstance()

    fun login() {
        val current = _uiState.value
        if (current.email.isBlank() || current.password.isBlank()) {
            _uiState.update { it.copy(error = "Enter email and password") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        auth.signInWithEmailAndPassword(current.email.trim(), current.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = task.exception?.localizedMessage ?: "Login failed"
                        )
                    }
                }
            }
    }
}

