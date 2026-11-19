package com.lafamilia.app.domain

/**
 * Pure rules for message status (issue #45), kept apart from Firestore so they can be unit tested.
 *
 * The receiver's phone writes two timestamps on the message document: `deliveredAt` when the
 * message reaches it, and `readAt` when the chat is opened. The sender's phone turns those
 * into DELIVERED / READ. A status only ever moves forward, so an old or out-of-order update
 * can never turn "Read" back into "Delivered".
 */
object MessageStatusRules {

    private fun rank(status: MessageStatus): Int = when (status) {
        MessageStatus.FAILED, MessageStatus.QUEUED -> 0
        MessageStatus.SENDING -> 1
        MessageStatus.SENT -> 2
        MessageStatus.DELIVERED -> 3
        MessageStatus.READ -> 4
    }

    /** Status of a message that exists on the server, from the receiver's receipts. */
    fun fromReceipts(deliveredAt: Long?, readAt: Long?): MessageStatus = when {
        readAt != null -> MessageStatus.READ
        deliveredAt != null -> MessageStatus.DELIVERED
        else -> MessageStatus.SENT
    }

    /** The newer of the two; never goes backwards. */
    fun advance(current: MessageStatus, incoming: MessageStatus): MessageStatus =
        if (rank(incoming) > rank(current)) incoming else current

    /** True once the server has the message, so the outbox must not send it again. */
    fun isOnServer(status: MessageStatus): Boolean = rank(status) >= rank(MessageStatus.SENT)
}
