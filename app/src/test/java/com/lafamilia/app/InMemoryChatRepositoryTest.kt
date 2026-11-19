package com.lafamilia.app

import com.lafamilia.app.data.InMemoryChatRepository
import com.lafamilia.app.domain.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryChatRepositoryTest {

    private var time = 1_000_000L
    private val repository = InMemoryChatRepository(clock = { time++ })

    @Test
    fun sendMessageAppendsAndUpdatesPreview() = runBlocking {
        repository.sendMessage("demo-mom", "hello")
        val messages = repository.observeMessages("demo-mom").first()
        assertEquals("hello", messages.last().body)
        val preview = repository.observeConversations().first().first { it.id == "demo-mom" }.lastMessageText
        assertEquals("hello", preview)
    }

    @Test
    fun editChangesOnlyTheBody() = runBlocking {
        val original = repository.observeMessages("demo-mom").first().first()
        repository.editMessage(original, "changed")
        val updated = repository.observeMessages("demo-mom").first().first { it.id == original.id }
        assertEquals("changed", updated.body)
    }

    @Test
    fun deleteRemovesTheMessage() = runBlocking {
        val original = repository.observeMessages("demo-mom").first().first()
        repository.deleteMessage(original)
        assertTrue(repository.observeMessages("demo-mom").first().none { it.id == original.id })
    }

    @Test
    fun markReadClearsUnread() = runBlocking {
        repository.markConversationRead("demo-mom")
        val unread = repository.observeConversations().first().first { it.id == "demo-mom" }.unreadCount
        assertEquals(0, unread)
    }

    @Test
    fun startDirectChatReusesTheSameConversation() = runBlocking {
        val user = User("u-mona", "Mona")
        val first = repository.startDirectChat(user)
        val second = repository.startDirectChat(user)
        assertEquals(first, second)
        assertEquals(1, repository.observeConversations().first().count { it.id == first })
    }

    @Test
    fun searchFindsByPrefixIgnoringCase() = runBlocking {
        assertEquals(listOf("Mona"), repository.searchUsers("mo").map { it.name })
        assertTrue(repository.searchUsers("   ").isEmpty())
    }

    @Test
    fun leaveRemovesConversation() = runBlocking {
        repository.leaveConversation("demo-dad")
        assertTrue(repository.observeConversations().first().none { it.id == "demo-dad" })
    }

    @Test
    fun pinMuteAndArchiveAreSavedAndCanBeUndone() = runBlocking {
        repository.setPinned("demo-dad", true)
        repository.setMuted("demo-dad", true)
        repository.setArchived("demo-dad", true)
        var dad = repository.observeConversations().first().first { it.id == "demo-dad" }
        assertTrue(dad.isPinned && dad.isMuted && dad.isArchived)

        repository.setPinned("demo-dad", false)
        repository.setMuted("demo-dad", false)
        repository.setArchived("demo-dad", false)
        dad = repository.observeConversations().first().first { it.id == "demo-dad" }
        assertTrue(!dad.isPinned && !dad.isMuted && !dad.isArchived)
    }
}
