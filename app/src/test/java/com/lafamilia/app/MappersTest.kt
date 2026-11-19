package com.lafamilia.app

import com.lafamilia.app.data.local.ConversationEntity
import com.lafamilia.app.data.local.MessageEntity
import com.lafamilia.app.data.local.parseStatus
import com.lafamilia.app.data.local.toDomain
import com.lafamilia.app.data.local.toEntity
import com.lafamilia.app.domain.Message
import com.lafamilia.app.domain.MessageStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class MappersTest {

    @Test
    fun messageSurvivesRoundTrip() {
        val message = Message("m1", "c1", "u1", "hello", MessageStatus.QUEUED, 42L)
        assertEquals(message, message.toEntity().toDomain())
    }

    @Test
    fun unknownStatusFallsBackToSent() {
        assertEquals(MessageStatus.SENT, parseStatus("SOMETHING_NEW"))
    }

    @Test
    fun everyStatusCanBeParsedBack() {
        MessageStatus.entries.forEach { assertEquals(it, parseStatus(it.name)) }
    }

    @Test
    fun conversationEntityMapsAllFields() {
        val entity = ConversationEntity("c1", "Mom", "hi", 10L, 3)
        val model = entity.toDomain()
        assertEquals("Mom", model.title)
        assertEquals(3, model.unreadCount)
        assertEquals(false, model.isPinned)
    }

    @Test
    fun conversationEntityMapsPinMuteArchive() {
        val model = ConversationEntity("c1", "Mom", "hi", 10L, 0, pinned = true, muted = true, archived = true).toDomain()
        assertEquals(true, model.isPinned)
        assertEquals(true, model.isMuted)
        assertEquals(true, model.isArchived)
    }

    @Test
    fun entityKeepsStatusAsText() {
        val entity: MessageEntity = Message("m", "c", "u", "b", MessageStatus.FAILED, 1L).toEntity()
        assertEquals("FAILED", entity.status)
    }
}
