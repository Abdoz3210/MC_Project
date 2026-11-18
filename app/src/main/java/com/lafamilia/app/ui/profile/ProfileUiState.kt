package com.lafamilia.app.ui.profile

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val status: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val nameEmpty: Boolean = false,
    val saved: Boolean = false,
    val saveFailed: Boolean = false
)
