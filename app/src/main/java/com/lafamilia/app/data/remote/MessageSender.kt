package com.lafamilia.app.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.lafamilia.app.data.local.AppDatabase
import com.lafamilia.app.domain.MessageStatus
import com.lafamilia.app.domain.MessageStatusRules
import com.lafamilia.app.data.local.parseStatus
import com.lafamilia.app.util.await
import kotlinx.coroutines.withTimeout

enum class SendResult { DONE, RETRY }

/**
 * Delivers one locally stored message to Firestore. Called by SendMessageWorker.
 * Writing a document with a fixed id is idempotent, so retrying can never create duplicates.
 */
class MessageSender(
    private val database: AppDatabase,
    private val firestore: FirebaseFirestore
) {

    suspend fun send(messageId: String): SendResult {
        val messageDao = database.messageDao()
        val message = messageDao.get(messageId) ?: return SendResult.DONE // deleted meanwhile
        if (MessageStatusRules.isOnServer(parseStatus(message.status))) return SendResult.DONE

        messageDao.setStatus(messageId, MessageStatus.SENDING.name)
        return try {
            val conversation = firestore.collection(CONVERSATIONS).document(message.conversationId)
            val data = mapOf(
                "senderId" to message.senderId,
                "body" to message.body,
                "createdAt" to message.createdAt
            )
            val batch = firestore.batch()
            batch.set(conversation.collection(MESSAGES).document(message.id), data)
            batch.set(
                conversation,
                mapOf("lastMessageText" to message.body, "lastMessageAt" to message.createdAt),
                SetOptions.merge()
            )
            withTimeout(TIMEOUT_MS) { batch.commit().await() }
            messageDao.markSentIfPending(messageId)
            SendResult.DONE
        } catch (e: Exception) {
            messageDao.setStatus(messageId, MessageStatus.QUEUED.name)
            SendResult.RETRY
        }
    }

    suspend fun markFailed(messageId: String) {
        database.messageDao().setStatus(messageId, MessageStatus.FAILED.name)
    }

    companion object {
        const val CONVERSATIONS = "conversations"
        const val MESSAGES = "messages"
        /** Receipts written by the receiver's phone (issue #45). */
        const val DELIVERED_AT = "deliveredAt"
        const val READ_AT = "readAt"
        private const val TIMEOUT_MS = 15_000L
    }
}
