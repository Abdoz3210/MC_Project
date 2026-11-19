package com.lafamilia.app.ui.chatlist

import com.lafamilia.app.domain.Conversation

/** Pure search and ordering logic for the list, kept out of the UI so it can be unit tested. */
object ConversationFilter {

    /** Keeps the conversations whose title or last message contains [query] (ignoring case). */
    fun filter(conversations: List<Conversation>, query: String): List<Conversation> {
        val text = query.trim()
        if (text.isEmpty()) return conversations
        return conversations.filter {
            it.title.contains(text, ignoreCase = true) || it.lastMessageText.contains(text, ignoreCase = true)
        }
    }

    /**
     * What the list shows: either the main list or the archived section, filtered by [query],
     * pinned chats first, then newest activity first.
     */
    fun arrange(conversations: List<Conversation>, query: String, archived: Boolean): List<Conversation> =
        filter(conversations.filter { it.isArchived == archived }, query)
            .sortedWith(compareByDescending<Conversation> { it.isPinned }.thenByDescending { it.lastMessageAt })
}
