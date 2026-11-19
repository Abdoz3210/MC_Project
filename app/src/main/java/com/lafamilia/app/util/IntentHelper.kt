package com.lafamilia.app.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri

/** Walks up the Context wrappers until it finds the Activity (Compose may hand us a wrapper). */
fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

/**
 * Implicit Intents: we describe WHAT we want (share text, write an email) and Android shows
 * the apps that can do it. Both functions return false when no app can handle the request.
 */
object IntentHelper {

    /** ACTION_SEND wrapped in a chooser: the user picks WhatsApp, Messages, Mail, etc. */
    fun shareText(context: Context, title: String, text: String): Boolean {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        return start(context, Intent.createChooser(send, title))
    }

    /** ACTION_SENDTO with a mailto: URI opens only email apps, with the fields pre-filled. */
    fun sendEmail(context: Context, address: String, subject: String, body: String): Boolean {
        val email = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        return start(context, email)
    }

    private fun start(context: Context, intent: Intent): Boolean {
        val host = context.findActivity() ?: context
        if (host !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            host.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
