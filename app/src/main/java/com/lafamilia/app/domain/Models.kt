package com.lafamilia.app.domain

/** Delivery state of a message. QUEUED and FAILED only exist on the sender's phone. */
enum class MessageStatus { QUEUED, SENDING, SENT, DELIVERED, READ, FAILED }

data class User(
    val id: String,
    val name: String,
    val username: String = "",
    val status: String = "",
    val avatarUrl: String? = null
)

data class Conversation(
    val id: String,
    val title: String,
    val lastMessageText: String,
    val lastMessageAt: Long,
    val unreadCount: Int,
    /** Per-user choices (issue #42): each member pins, mutes or archives a chat for themselves only. */
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isArchived: Boolean = false
)

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val body: String,
    val status: MessageStatus,
    val createdAt: Long
)
