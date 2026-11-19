package com.lafamilia.app.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeFormat {

    /**
     * Short time label for the conversation list:
     * today -> "14:05", this year -> "17 Nov", older -> "17 Nov 2025", unknown time -> "".
     * [now], [zone] and [locale] are parameters so tests do not depend on the real clock.
     */
    fun chatTime(
        timestamp: Long,
        now: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault()
    ): String {
        if (timestamp <= 0L) return ""
        val moment = Instant.ofEpochMilli(timestamp).atZone(zone)
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val pattern = when {
            moment.toLocalDate() == today -> "HH:mm"
            moment.year == today.year -> "d MMM"
            else -> "d MMM yyyy"
        }
        return DateTimeFormatter.ofPattern(pattern, locale).format(moment)
    }
}
