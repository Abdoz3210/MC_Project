package com.lafamilia.app.domain

import kotlinx.coroutines.flow.Flow

/**
 * The contract between the UI and the data layer.
 *
 * Screens only know this interface. The app uses RoomChatRepository (Room as source of
 * truth + Firestore sync); the unit tests use an in-memory fake (src/test, InMemoryChatRepository).
 */
interface ChatRepository {

    /** Id of the signed-in user (empty when nobody is signed in). */
    val currentUserId: String

    /** Conversations of the current user, newest activity first. Emits again on every change. */
    fun observeConversations(): Flow<List<Conversation>>

    /** Messages of one conversation, oldest first. */
    fun observeMessages(conversationId: String): Flow<List<Message>>

    fun observeConversationTitle(conversationId: String): Flow<String>

    /** Finds users whose name starts with [query]. The current user is never returned. */
    suspend fun searchUsers(query: String): List<User>

    /** Creates (or reuses) the one-to-one conversation with [other] and returns its id. */
    suspend fun startDirectChat(other: User): String

    /** Create: stores the message locally as QUEUED, then delivers it when possible. */
    suspend fun sendMessage(conversationId: String, body: String)

    /** Update: changes the text of one of your own messages. */
    suspend fun editMessage(message: Message, newBody: String)

    /** Delete: removes one of your own messages. */
    suspend fun deleteMessage(message: Message)

    /** Tries to deliver a FAILED message again. */
    suspend fun retryMessage(message: Message)

    /** Marks everything in the conversation as read (resets the unread counter). */
    suspend fun markConversationRead(conversationId: String)

    /** Removes the conversation from the current user's list. */
    suspend fun leaveConversation(conversationId: String)

    /** Pinned chats stay at the top of the list. Only affects the current user. */
    suspend fun setPinned(conversationId: String, pinned: Boolean)

    /** Muted chats still receive messages but do not highlight their unread badge. */
    suspend fun setMuted(conversationId: String, muted: Boolean)

    /** Archived chats move out of the main list into the "Archived" section. */
    suspend fun setArchived(conversationId: String, archived: Boolean)
}
