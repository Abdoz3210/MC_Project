package com.lafamilia.app.data.remote

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.lafamilia.app.data.local.AppDatabase
import com.lafamilia.app.data.local.ConversationEntity
import com.lafamilia.app.data.local.MessageEntity
import com.lafamilia.app.domain.MessageStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Firestore -> Room. It listens to the server and copies every change into the local
 * database. The UI never reads Firestore directly; it only reads Room, so the app keeps
 * working offline and shows exactly what was last synced.
 */
class FirestoreSync(
    private val database: AppDatabase,
    private val firestore: FirebaseFirestore,
    private val scope: CoroutineScope
) {

    private var conversationListener: ListenerRegistration? = null
    private val messageListeners = mutableMapOf<String, ListenerRegistration>()
    private var userId: String = ""

    fun start(uid: String) {
        stop()
        userId = uid
        conversationListener = firestore.collection(MessageSender.CONVERSATIONS)
            .whereArrayContains("members", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch { handleConversations(snapshot) }
            }
    }

    fun stop() {
        conversationListener?.remove()
        conversationListener = null
        synchronized(messageListeners) {
            messageListeners.values.forEach { it.remove() }
            messageListeners.clear()
        }
    }

    private suspend fun handleConversations(snapshot: QuerySnapshot) {
        for (change in snapshot.documentChanges) {
            val id = change.document.id
            if (change.type == DocumentChange.Type.REMOVED) {
                stopListeningTo(id)
                database.messageDao().deleteForConversation(id)
                database.conversationDao().delete(id)
            } else {
                upsertConversation(change.document)
                listenToMessages(id)
            }
        }
    }

    private suspend fun upsertConversation(document: DocumentSnapshot) {
        val dao = database.conversationDao()
        val members = (document.get("members") as? List<*>)?.filterIsInstance<String>().orEmpty()
        val names = document.get("memberNames") as? Map<*, *>
        val otherId = members.firstOrNull { it != userId }
        val title = (names?.get(otherId) as? String).orEmpty().ifBlank { "Chat" }
        val existing = dao.get(document.id)
        dao.upsert(
            ConversationEntity(
                id = document.id,
                title = title,
                lastMessageText = document.getString("lastMessageText").orEmpty(),
                lastMessageAt = document.getLong("lastMessageAt") ?: 0L,
                unreadCount = existing?.unreadCount ?: 0
            )
        )
    }

    private fun listenToMessages(conversationId: String) {
        synchronized(messageListeners) {
            if (messageListeners.containsKey(conversationId)) return
            messageListeners[conversationId] = firestore.collection(MessageSender.CONVERSATIONS)
                .document(conversationId)
                .collection(MessageSender.MESSAGES)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    scope.launch { handleMessages(conversationId, snapshot) }
                }
        }
    }

    private fun stopListeningTo(conversationId: String) {
        synchronized(messageListeners) { messageListeners.remove(conversationId)?.remove() }
    }

    private suspend fun handleMessages(conversationId: String, snapshot: QuerySnapshot) {
        val messages = database.messageDao()
        for (change in snapshot.documentChanges) {
            val document = change.document
            when (change.type) {
                DocumentChange.Type.REMOVED -> messages.delete(document.id)
                DocumentChange.Type.MODIFIED -> messages.setBody(document.id, document.getString("body").orEmpty())
                DocumentChange.Type.ADDED -> {
                    val existing = messages.get(document.id)
                    val sender = document.getString("senderId").orEmpty()
                    if (existing != null) {
                        // Our own message coming back from the server: it is delivered now.
                        if (existing.status != MessageStatus.SENT.name) messages.setStatus(existing.id, MessageStatus.SENT.name)
                    } else {
                        messages.upsert(
                            MessageEntity(
                                id = document.id,
                                conversationId = conversationId,
                                senderId = sender,
                                body = document.getString("body").orEmpty(),
                                status = MessageStatus.SENT.name,
                                createdAt = document.getLong("createdAt") ?: 0L
                            )
                        )
                        if (sender != userId) database.conversationDao().addUnread(conversationId, 1)
                    }
                }
            }
        }
    }
}
