package com.lafamilia.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lafamilia.app.AppGraph
import com.lafamilia.app.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository = AppGraph.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = authRepository.loadProfile()
            _uiState.update {
                it.copy(name = profile.name, email = profile.email, status = profile.status, isLoading = false)
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameEmpty = false, saved = false, saveFailed = false) }
    }

    fun onStatusChange(value: String) {
        _uiState.update { it.copy(status = value, saved = false, saveFailed = false) }
    }

    fun save() {
        val current = _uiState.value
        if (current.isSaving) return
        if (current.name.isBlank()) {
            _uiState.update { it.copy(nameEmpty = true) }
            return
        }

        _uiState.update { it.copy(isSaving = true, saved = false, saveFailed = false) }
        viewModelScope.launch {
            authRepository.updateProfile(current.name.trim(), current.status.trim())
                .onSuccess { _uiState.update { it.copy(isSaving = false, saved = true) } }
                .onFailure { _uiState.update { it.copy(isSaving = false, saveFailed = true) } }
        }
    }

    fun signOut() = authRepository.signOut()
}
