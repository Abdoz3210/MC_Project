package com.lafamilia.app.ui.auth

import com.lafamilia.app.domain.AuthError

/**
 * Everything the login screen shows, in one immutable object.
 * [validationError] comes from our own rules, [serverError] is Firebase's message.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val validationError: AuthError? = null,
    val serverError: String? = null,
    val isLoggedIn: Boolean = false
)
