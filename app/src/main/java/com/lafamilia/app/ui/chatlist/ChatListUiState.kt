package com.lafamilia.app.ui.chatlist

import com.lafamilia.app.domain.Conversation
import com.lafamilia.app.domain.User

/** State of the "start a new chat" dialog. null in [ChatListUiState] means the dialog is closed. */
data class NewChatState(
    val query: String = "",
    val results: List<User> = emptyList(),
    val isSearching: Boolean = false,
    val failed: Boolean = false
)

data class ChatListUiState(
    val conversations: List<Conversation> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true,
    val newChat: NewChatState? = null,
    val pendingDelete: Conversation? = null,
    /** The chat whose long-press menu (pin, mute, archive, remove) is open. */
    val menuFor: Conversation? = null,
    /** true while the "Archived" section is shown instead of the main list. */
    val showArchived: Boolean = false,
    /** One-shot navigation request: set when a chat should open, cleared once it did. */
    val openChatId: String? = null
) {
    val visibleConversations: List<Conversation>
        get() = ConversationFilter.arrange(conversations, query, archived = showArchived)

    val archivedCount: Int
        get() = conversations.count { it.isArchived }
}
