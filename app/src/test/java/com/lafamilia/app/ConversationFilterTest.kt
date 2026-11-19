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
}
