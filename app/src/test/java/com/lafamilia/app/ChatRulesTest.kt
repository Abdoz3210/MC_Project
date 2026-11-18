package com.lafamilia.app

import com.lafamilia.app.domain.Message
import com.lafamilia.app.domain.MessageStatus
import com.lafamilia.app.ui.chat.ChatRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatRulesTest {

    private fun message(sender: String, status: MessageStatus = MessageStatus.SENT) =
        Message("1", "c", sender, "hi", status, 0L)

    @Test
    fun blankTextIsNotSent() {
        assertNull(ChatRules.cleanBody("   \n "))
    }

    @Test
    fun textIsTrimmed() {
        assertEquals("hello", ChatRules.cleanBody("  hello  "))
    }

    @Test
    fun longTextIsCut() {
        val long = "a".repeat(ChatRules.MAX_LENGTH + 50)
        assertEquals(ChatRules.MAX_LENGTH, ChatRules.cleanBody(long)!!.length)
    }

    @Test
    fun onlyOwnMessagesCanBeModified() {
        assertTrue(ChatRules.canModify(message("me"), "me"))
        assertFalse(ChatRules.canModify(message("other"), "me"))
    }

    @Test
    fun onlyOwnFailedMessagesCanBeRetried() {
        assertTrue(ChatRules.canRetry(message("me", MessageStatus.FAILED), "me"))
        assertFalse(ChatRules.canRetry(message("me", MessageStatus.SENT), "me"))
        assertFalse(ChatRules.canRetry(message("other", MessageStatus.FAILED), "me"))
    }
}
