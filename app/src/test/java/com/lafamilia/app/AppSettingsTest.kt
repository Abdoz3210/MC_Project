package com.lafamilia.app

import com.lafamilia.app.data.settings.AppLanguage
import com.lafamilia.app.data.settings.AppSettings
import com.lafamilia.app.data.settings.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSettingsTest {

    @Test
    fun unknownThemeFallsBackToSystem() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromName("PURPLE"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromName(null))
    }

    @Test
    fun savedThemeIsRestored() {
        assertEquals(ThemeMode.DARK, ThemeMode.fromName("DARK"))
    }

    @Test
    fun languageTags() {
        assertEquals("ar", AppLanguage.fromName("ARABIC").localeTag)
        assertEquals("en", AppLanguage.fromName("ENGLISH").localeTag)
        assertNull(AppLanguage.fromName("garbage").localeTag)
    }

    @Test
    fun defaultsAreSystemAndNotificationsOn() {
        val defaults = AppSettings()
        assertEquals(ThemeMode.SYSTEM, defaults.theme)
        assertEquals(AppLanguage.SYSTEM, defaults.language)
        assertTrue(defaults.notificationsEnabled)
    }
}
