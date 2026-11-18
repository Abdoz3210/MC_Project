package com.lafamilia.app.ui.auth

import com.lafamilia.app.domain.AuthError
import com.lafamilia.app.domain.AuthValidator
import com.lafamilia.app.domain.PasswordStrength

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val validationError: AuthError? = null,
    val serverError: String? = null,
    val isRegistered: Boolean = false
) {
    /** Derived from the password, so it can never get out of sync with it. */
    val strength: PasswordStrength get() = AuthValidator.passwordStrength(password)
}
