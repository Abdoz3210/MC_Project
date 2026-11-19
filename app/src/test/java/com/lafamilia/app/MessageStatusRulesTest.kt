package com.lafamilia.app

import com.lafamilia.app.domain.MessageStatus
import com.lafamilia.app.domain.MessageStatusRules
import com.lafamilia.app.ui.chat.statusTicks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageStatusRulesTest {

    @Test
    fun noReceiptsMeansSent() {
        assertEquals(MessageStatus.SENT, MessageStatusRules.fromReceipts(deliveredAt = null, readAt = null))
    }

    @Test
    fun deliveredReceiptMeansDelivered() {
        assertEquals(MessageStatus.DELIVERED, MessageStatusRules.fromReceipts(deliveredAt = 5L, readAt = null))
    }

    @Test
    fun readWinsOverDelivered() {
        assertEquals(MessageStatus.READ, MessageStatusRules.fromReceipts(deliveredAt = 5L, readAt = 9L))
        // read without a delivered receipt (e.g. it was still queued offline) is still read
        assertEquals(MessageStatus.READ, MessageStatusRules.fromReceipts(deliveredAt = null, readAt = 9L))
    }

    @Test
    fun statusOnlyMovesForward() {
        assertEquals(MessageStatus.READ, MessageStatusRules.advance(MessageStatus.READ, MessageStatus.DELIVERED))
        assertEquals(MessageStatus.DELIVERED, MessageStatusRules.advance(MessageStatus.SENT, MessageStatus.DELIVERED))
        assertEquals(MessageStatus.SENT, MessageStatusRules.advance(MessageStatus.SENDING, MessageStatus.SENT))
    }

    @Test
    fun aMessageOnTheServerIsNeverSentAgain() {
        assertTrue(MessageStatusRules.isOnServer(MessageStatus.SENT))
        assertTrue(MessageStatusRules.isOnServer(MessageStatus.DELIVERED))
        assertTrue(MessageStatusRules.isOnServer(MessageStatus.READ))
        assertFalse(MessageStatusRules.isOnServer(MessageStatus.QUEUED))
        assertFalse(MessageStatusRules.isOnServer(MessageStatus.FAILED))
    }

    @Test
    fun ticksMatchTheStatus() {
        assertEquals("✓", statusTicks(MessageStatus.SENT))
        assertEquals("✓✓", statusTicks(MessageStatus.DELIVERED))
        assertEquals("✓✓", statusTicks(MessageStatus.READ))
        assertEquals("!", statusTicks(MessageStatus.FAILED))
    }
}
