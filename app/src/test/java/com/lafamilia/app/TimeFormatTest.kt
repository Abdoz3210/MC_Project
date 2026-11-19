package com.lafamilia.app

import com.lafamilia.app.util.TimeFormat
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeFormatTest {

    private val utc = ZoneOffset.UTC
    private val now = ZonedDateTime.of(2026, 11, 18, 12, 0, 0, 0, utc).toInstant().toEpochMilli()

    private fun millis(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0): Long =
        ZonedDateTime.of(year, month, day, hour, minute, 0, 0, utc).toInstant().toEpochMilli()

    private fun format(timestamp: Long) = TimeFormat.chatTime(timestamp, now, utc, Locale.ENGLISH)

    @Test
    fun todayShowsClockTime() {
        assertEquals("09:05", format(millis(2026, 11, 18, 9, 5)))
    }

    @Test
    fun earlierThisYearShowsDayAndMonth() {
        assertEquals("17 Nov", format(millis(2026, 11, 17, 23, 0)))
    }

    @Test
    fun olderYearShowsYearToo() {
        assertEquals("5 Jan 2025", format(millis(2025, 1, 5)))
    }

    @Test
    fun unknownTimeIsEmpty() {
        assertEquals("", format(0L))
    }
}
