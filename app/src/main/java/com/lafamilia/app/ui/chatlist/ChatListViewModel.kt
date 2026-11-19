package com.lafamilia.app.ui.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lafamilia.app.AppGraph
import com.lafamilia.app.domain.ChatRepository
import com.lafamilia.app.domain.Conversation
import com.lafamilia.app.domain.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val repository: ChatRepository = AppGraph.chatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            repository.observeConversations().collect { list ->
                _uiState.update { it.copy(conversations = list, isLoading = false) }
            }
        }
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value) }
    }

    // ---- new chat dialog -------------------------------------------------------------

    fun openNewChat() {
        _uiState.update { it.copy(newChat = NewChatState()) }
    }

    fun closeNewChat() {
        searchJob?.cancel()
        _uiState.update { it.copy(newChat = null) }
    }

    /** Waits for a short pause in typing before asking the server, so we do not search on every key. */
    fun onUserQueryChange(value: String) {
        searchJob?.cancel()
        if (value.isBlank()) {
            updateNewChat { it.copy(query = value, results = emptyList(), isSearching = false, failed = false) }
            return
        }
        updateNewChat { it.copy(query = value, isSearching = true, failed = false) }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            try {
                val users = repository.searchUsers(value)
                updateNewChat { it.copy(results = users, isSearching = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                updateNewChat { it.copy(isSearching = false, failed = true) }
            }
        }
    }

    fun startChatWith(user: User) {
        viewModelScope.launch {
            try {
                val conversationId = repository.startDirectChat(user)
                _uiState.update { it.copy(newChat = null, openChatId = conversationId) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                updateNewChat { it.copy(failed = true) }
            }
        }
    }

    fun onChatOpened() {
        _uiState.update { it.copy(openChatId = null) }
    }

    // ---- long-press menu: pin, mute, archive (issue #42) ------------------------------

    fun openMenu(conversation: Conversation) {
        _uiState.update { it.copy(menuFor = conversation) }
    }

    fun closeMenu() {
        _uiState.update { it.copy(menuFor = null) }
    }

    fun togglePin() = withMenuTarget { repository.setPinned(it.id, !it.isPinned) }

    fun toggleMute() = withMenuTarget { repository.setMuted(it.id, !it.isMuted) }

    fun toggleArchive() = withMenuTarget { repository.setArchived(it.id, !it.isArchived) }

    /** "Remove" in the menu opens the existing confirmation dialog. */
    fun removeFromMenu() {
        val target = _uiState.value.menuFor ?: return
        _uiState.update { it.copy(menuFor = null, pendingDelete = target) }
    }

    fun showArchived(show: Boolean) {
        _uiState.update { it.copy(showArchived = show, query = "") }
    }

    private fun withMenuTarget(action: suspend (Conversation) -> Unit) {
        val target = _uiState.value.menuFor ?: return
        _uiState.update { it.copy(menuFor = null) }
        viewModelScope.launch { action(target) }
    }

    // ---- remove a conversation -------------------------------------------------------

    fun askDelete(conversation: Conversation) {
        _uiState.update { it.copy(pendingDelete = conversation) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(pendingDelete = null) }
    }

    fun confirmDelete() {
        val target = _uiState.value.pendingDelete ?: return
        _uiState.update { it.copy(pendingDelete = null) }
        viewModelScope.launch { repository.leaveConversation(target.id) }
    }

    private fun updateNewChat(change: (NewChatState) -> NewChatState) {
        _uiState.update { state -> state.copy(newChat = state.newChat?.let(change)) }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
