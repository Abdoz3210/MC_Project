package com.lafamilia.app.ui.chat

import com.lafamilia.app.domain.Message

data class ChatUiState(
    val title: String = "",
    val messages: List<Message> = emptyList(),
    val draft: String = "",
    val currentUserId: String = "",
    /** Message being edited; the composer is then in "edit mode". */
    val editing: Message? = null,
    /** Message the user long-pressed; shows the action menu. */
    val selected: Message? = null,
    val pendingDelete: Message? = null
)
