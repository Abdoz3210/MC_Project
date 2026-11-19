package com.lafamilia.app.ui.chat

import com.lafamilia.app.domain.Message
import com.lafamilia.app.domain.MessageStatus

/** Pure rules about messages, kept out of the UI so they can be unit tested. */
object ChatRules {

    const val MAX_LENGTH = 2000

    /** Text that will actually be stored: trimmed, or null when there is nothing to send. */
    fun cleanBody(raw: String): String? {
        val text = raw.trim()
        return if (text.isEmpty()) null else text.take(MAX_LENGTH)
    }

    fun isMine(message: Message, currentUserId: String): Boolean = message.senderId == currentUserId

    /** Only your own messages can be edited or deleted. */
    fun canModify(message: Message, currentUserId: String): Boolean = isMine(message, currentUserId)

    /** A failed message can be sent again. */
    fun canRetry(message: Message, currentUserId: String): Boolean =
        isMine(message, currentUserId) && message.status == MessageStatus.FAILED
}
