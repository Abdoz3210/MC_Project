package com.lafamilia.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** One row per conversation in the local database. */
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val lastMessageText: String,
    val lastMessageAt: Long,
    val unreadCount: Int
)

/** One row per message. [status] stores a MessageStatus name, e.g. "QUEUED". */
@Entity(tableName = "messages", indices = [Index("conversationId")])
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val body: String,
    val status: String,
    val createdAt: Long
)
