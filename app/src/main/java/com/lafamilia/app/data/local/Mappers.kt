package com.lafamilia.app.data.local

import com.lafamilia.app.domain.Conversation
import com.lafamilia.app.domain.Message
import com.lafamilia.app.domain.MessageStatus

/** Converts between database rows (entities) and the plain models the UI uses. */

fun ConversationEntity.toDomain() = Conversation(
    id = id,
    title = title,
    lastMessageText = lastMessageText,
    lastMessageAt = lastMessageAt,
    unreadCount = unreadCount,
    isPinned = pinned,
    isMuted = muted,
    isArchived = archived
)

fun MessageEntity.toDomain() = Message(
    id = id,
    conversationId = conversationId,
    senderId = senderId,
    body = body,
    status = parseStatus(status),
    createdAt = createdAt
)

fun Message.toEntity() = MessageEntity(
    id = id,
    conversationId = conversationId,
    senderId = senderId,
    body = body,
    status = status.name,
    createdAt = createdAt
)

/** Unknown text (for example from a future app version) falls back to SENT instead of crashing. */
fun parseStatus(name: String): MessageStatus =
    MessageStatus.entries.firstOrNull { it.name == name } ?: MessageStatus.SENT
