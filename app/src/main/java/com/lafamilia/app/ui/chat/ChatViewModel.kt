package com.lafamilia.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lafamilia.app.AppGraph
import com.lafamilia.app.domain.ChatRepository
import com.lafamilia.app.domain.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val conversationId: String,
    private val repository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState(currentUserId = repository.currentUserId))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeMessages(conversationId).collect { list ->
                _uiState.update { it.copy(messages = list) }
                // The chat is open, so anything that arrives now is already read.
                repository.markConversationRead(conversationId)
            }
        }
        viewModelScope.launch {
            repository.observeConversationTitle(conversationId).collect { title ->
                _uiState.update { it.copy(title = title) }
            }
        }
    }

    fun onDraftChange(value: String) {
        _uiState.update { it.copy(draft = value) }
    }

    /** Send button: creates a new message, or saves the edit when in edit mode. */
    fun submit() {
        val state = _uiState.value
        val body = ChatRules.cleanBody(state.draft) ?: return
        val target = state.editing
        _uiState.update { it.copy(draft = "", editing = null) }
        viewModelScope.launch {
            if (target == null) repository.sendMessage(conversationId, body)
            else repository.editMessage(target, body)
        }
    }

    // ---- long-press menu ------------------------------------------------------------

    fun select(message: Message) {
        _uiState.update { it.copy(selected = message) }
    }

    fun dismissMenu() {
        _uiState.update { it.copy(selected = null) }
    }

    fun startEdit() {
        val message = _uiState.value.selected ?: return
        _uiState.update { it.copy(editing = message, draft = message.body, selected = null) }
    }

    fun cancelEdit() {
        _uiState.update { it.copy(editing = null, draft = "") }
    }

    fun askDelete() {
        val message = _uiState.value.selected ?: return
        _uiState.update { it.copy(pendingDelete = message, selected = null) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(pendingDelete = null) }
    }

    fun confirmDelete() {
        val message = _uiState.value.pendingDelete ?: return
        _uiState.update { it.copy(pendingDelete = null) }
        viewModelScope.launch { repository.deleteMessage(message) }
    }

    fun retry(message: Message) {
        viewModelScope.launch { repository.retryMessage(message) }
    }

    companion object {
        /** A ViewModel with constructor arguments needs a factory; this builds one per conversation. */
        fun factory(conversationId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer { ChatViewModel(conversationId, AppGraph.chatRepository) }
        }
    }
}
