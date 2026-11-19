package com.lafamilia.app.util

/** Pure text builders for the share and feedback Intents, so they can be unit tested. */
object InviteText {

    /** Change this to the team's real mailbox before the demo. */
    const val FEEDBACK_EMAIL = "abdelrhmansalah108@gmail.com"

    const val APP_LINK = "https://github.com/Abdoz3210/MC_Project"

    fun invitation(appName: String, senderName: String): String {
        val who = senderName.trim()
        val greeting = if (who.isEmpty()) "Hey!" else "Hey! It's $who."
        return "$greeting Join me on $appName, a simple chat app for staying close to family and friends. $APP_LINK"
    }

    fun feedbackSubject(appName: String): String = "$appName feedback"

    fun feedbackBody(appVersion: String, androidVersion: Int): String =
        "\n\n---\nApp version: $appVersion\nAndroid: $androidVersion"
}
