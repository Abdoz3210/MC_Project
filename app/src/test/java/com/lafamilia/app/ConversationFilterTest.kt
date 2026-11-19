package com.lafamilia.app

import com.lafamilia.app.domain.Conversation
import com.lafamilia.app.ui.chatlist.ConversationFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class ConversationFilterTest {

    private val mom = Conversation("1", "Mom", "See you at dinner", 10L, 0)
    private val dad = Conversation("2", "Dad", "Call me", 5L, 1)
    private val all = listOf(mom, dad)

    @Test
    fun blankQueryKeepsEverything() {
        assertEquals(all, ConversationFilter.filter(all, "   "))
    }

    @Test
    fun matchesTitleIgnoringCase() {
        assertEquals(listOf(mom), ConversationFilter.filter(all, "mOm"))
    }

    @Test
    fun matchesLastMessage() {
        assertEquals(listOf(dad), ConversationFilter.filter(all, "call"))
    }

    @Test
    fun noMatchGivesEmptyList() {
        assertEquals(emptyList<Conversation>(), ConversationFilter.filter(all, "zzz"))
    }

    // ---- arrange: pin, archive (issue #42) ----

    private val sara = Conversation("3", "Sara", "Hi", 20L, 0)

    @Test
    fun newestFirstWhenNothingIsPinned() {
        assertEquals(listOf(sara, mom, dad), ConversationFilter.arrange(listOf(dad, mom, sara), "", archived = false))
    }

    @Test
    fun pinnedChatsComeFirstEvenWhenOlder() {
        val pinnedDad = dad.copy(isPinned = true)
        assertEquals(listOf(pinnedDad, sara, mom), ConversationFilter.arrange(listOf(mom, pinnedDad, sara), "", archived = false))
    }

    @Test
    fun archivedChatsOnlyAppearInTheArchivedSection() {
        val archivedMom = mom.copy(isArchived = true)
        val list = listOf(archivedMom, dad, sara)
        assertEquals(listOf(sara, dad), ConversationFilter.arrange(list, "", archived = false))
        assertEquals(listOf(archivedMom), ConversationFilter.arrange(list, "", archived = true))
    }

    @Test
    fun searchStillWorksInsideTheArchivedSection() {
        val list = listOf(mom.copy(isArchived = true), dad.copy(isArchived = true))
        assertEquals(listOf("Dad"), ConversationFilter.arrange(list, "call", archived = true).map { it.title })
    }
}
