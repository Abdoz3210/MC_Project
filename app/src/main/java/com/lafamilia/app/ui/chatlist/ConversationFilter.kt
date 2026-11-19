package com.lafamilia.app.ui.chatlist

import com.lafamilia.app.domain.Conversation

/** Pure search logic for the list, kept out of the UI so it can be unit tested. */
object ConversationFilter {

    /** Keeps the conversations whose title or last message contains [query] (ignoring case). */
    fun filter(conversations: List<Conversation>, query: String): List<Conversation> {
        val text = query.trim()
        if (text.isEmpty()) return conversations
        return conversations.filter {
            it.title.contains(text, ignoreCase = true) || it.lastMessageText.contains(text, ignoreCase = true)
        }
    }
}
