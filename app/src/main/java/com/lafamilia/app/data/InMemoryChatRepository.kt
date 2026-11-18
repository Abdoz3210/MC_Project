package com.lafamilia.app.data

import com.lafamilia.app.domain.ChatRepository
import com.lafamilia.app.domain.Conversation
import com.lafamilia.app.domain.Message
import com.lafamilia.app.domain.MessageStatus
import com.lafamilia.app.domain.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * A fake repository that keeps everything in memory. It lets every screen be built and
 * tested without a network or a database, and it is what the unit tests use.
 */
class InMemoryChatRepository(
    private val clock: () -> Long = System::currentTimeMillis
) : ChatRepository {

    override val currentUserId: String = ME

    private val conversations = MutableStateFlow(seedConversations())
    private val messages = MutableStateFlow(seedMessages())

    private val directory = listOf(
        User(id = "u-mona", name = "Mona", username = "mona"),
        User(id = "u-karim", name = "Karim", username = "karim"),
        User(id = "u-salma", name = "Salma", username = "salma")
    )

    override fun observeConversations(): Flow<List<Conversation>> =
        conversations.map { list -> list.sortedByDescending { it.lastMessageAt } }

    override fun observeMessages(conversationId: String): Flow<List<Message>> =
        messages.map { byConversation -> byConversation[conversationId].orEmpty().sortedBy { it.createdAt } }

    override fun observeConversationTitle(conversationId: String): Flow<String> =
        conversations.map { list -> list.firstOrNull { it.id == conversationId }?.title.orEmpty() }

    override suspend fun searchUsers(query: String): List<User> {
        val text = query.trim()
        if (text.isEmpty()) return emptyList()
        return directory.filter { it.name.startsWith(text, ignoreCase = true) }
    }

    override suspend fun startDirectChat(other: User): String {
        val id = listOf(ME, other.id).sorted().joinToString("_")
        conversations.update { list ->
            if (list.any { it.id == id }) list
            else list + Conversation(id = id, title = other.name, lastMessageText = "", lastMessageAt = 0L, unreadCount = 0)
        }
        return id
    }

    override suspend fun sendMessage(conversationId: String, body: String) {
        val message = Message(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = ME,
            body = body,
            status = MessageStatus.SENT,
            createdAt = clock()
        )
        messages.update { map -> map + (conversationId to map[conversationId].orEmpty() + message) }
        conversations.update { list ->
            list.map {
                if (it.id == conversationId) it.copy(lastMessageText = body, lastMessageAt = message.createdAt) else it
            }
        }
    }

    override suspend fun editMessage(message: Message, newBody: String) {
        replaceMessage(message.id) { it.copy(body = newBody) }
    }

    override suspend fun deleteMessage(message: Message) {
        messages.update { map ->
            map.mapValues { (_, list) -> list.filterNot { it.id == message.id } }
        }
    }

    override suspend fun retryMessage(message: Message) {
        replaceMessage(message.id) { it.copy(status = MessageStatus.SENT) }
    }

    override suspend fun markConversationRead(conversationId: String) {
        conversations.update { list ->
            list.map { if (it.id == conversationId) it.copy(unreadCount = 0) else it }
        }
    }

    override suspend fun leaveConversation(conversationId: String) {
        conversations.update { list -> list.filterNot { it.id == conversationId } }
        messages.update { map -> map - conversationId }
    }

    private fun replaceMessage(id: String, change: (Message) -> Message) {
        messages.update { map ->
            map.mapValues { (_, list) -> list.map { if (it.id == id) change(it) else it } }
        }
    }

    private fun seedConversations(): List<Conversation> {
        val now = clock()
        return listOf(
            Conversation("demo-mom", "Mom", "See you at dinner", now - 60_000, unreadCount = 2),
            Conversation("demo-dad", "Dad", "Call me when you can", now - 3_600_000, unreadCount = 0)
        )
    }

    private fun seedMessages(): Map<String, List<Message>> {
        val now = clock()
        return mapOf(
            "demo-mom" to listOf(
                Message("m1", "demo-mom", "u-mom", "Are you coming home today?", MessageStatus.SENT, now - 120_000),
                Message("m2", "demo-mom", ME, "Yes, after class", MessageStatus.SENT, now - 100_000),
                Message("m3", "demo-mom", "u-mom", "See you at dinner", MessageStatus.SENT, now - 60_000)
            ),
            "demo-dad" to listOf(
                Message("m4", "demo-dad", "u-dad", "Call me when you can", MessageStatus.SENT, now - 3_600_000)
            )
        )
    }

    companion object {
        const val ME = "me"
    }
}
