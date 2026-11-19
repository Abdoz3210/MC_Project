package com.lafamilia.app

import com.lafamilia.app.util.InviteText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InviteTextTest {

    @Test
    fun invitationMentionsSenderAppAndLink() {
        val text = InviteText.invitation("La Familia", "Abdos")
        assertTrue(text.contains("Abdos"))
        assertTrue(text.contains("La Familia"))
        assertTrue(text.contains(InviteText.APP_LINK))
    }

    @Test
    fun invitationWithoutNameStillReads() {
        val text = InviteText.invitation("La Familia", "  ")
        assertTrue(text.startsWith("Hey!"))
        assertTrue(!text.contains("It's"))
    }

    @Test
    fun feedbackSubjectUsesAppName() {
        assertEquals("La Familia feedback", InviteText.feedbackSubject("La Familia"))
    }

    @Test
    fun feedbackBodyListsVersions() {
        val body = InviteText.feedbackBody("1.0", 34)
        assertTrue(body.contains("App version: 1.0"))
        assertTrue(body.contains("Android: 34"))
    }
}
