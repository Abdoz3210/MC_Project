package com.lafamilia.app.data.remote

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.lafamilia.app.data.local.AppDatabase
import com.lafamilia.app.data.local.ConversationEntity
import com.lafamilia.app.data.local.MessageEntity
import com.lafamilia.app.data.local.parseStatus
import com.lafamilia.app.domain.MessageStatus
import com.lafamilia.app.domain.MessageStatusRules
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
                unreadCount = existing?.unreadCount ?: 0,
                pinned = document.hasMember(PINNED_BY, userId),
                muted = document.hasMember(MUTED_BY, userId),
                archived = document.hasMember(ARCHIVED_BY, userId)
            )
        )
    }

    /** True when the array field [field] (a list of user ids) contains [uid]. */
    private fun DocumentSnapshot.hasMember(field: String, uid: String): Boolean =
        (get(field) as? List<*>)?.contains(uid) == true

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
            val sender = document.getString("senderId").orEmpty()
            val mine = sender == userId
            if (change.type == DocumentChange.Type.REMOVED) {
                messages.delete(document.id)
                continue
            }
            val existing = messages.get(document.id)
            when {
                existing == null -> {
                    messages.upsert(
                        MessageEntity(
                            id = document.id,
                            conversationId = conversationId,
                            senderId = sender,
                            body = document.getString("body").orEmpty(),
                            status = if (mine) receiptStatus(document).name else MessageStatus.SENT.name,
                            createdAt = document.getLong("createdAt") ?: 0L
                        )
                    )
                    if (!mine) {
                        database.conversationDao().addUnread(conversationId, 1)
                        reportDelivered(document)
                    }
                }
                mine -> {
                    // Our own message: the receiver's receipts move it to DELIVERED / READ.
                    if (change.type == DocumentChange.Type.MODIFIED) messages.setBody(existing.id, document.getString("body").orEmpty())
                    val next = MessageStatusRules.advance(parseStatus(existing.status), receiptStatus(document))
                    if (next.name != existing.status) messages.setStatus(existing.id, next.name)
                }
                change.type == DocumentChange.Type.MODIFIED -> {
                    // Their message was edited.
                    messages.setBody(existing.id, document.getString("body").orEmpty())
                }
            }
        }
    }

    private fun receiptStatus(document: DocumentSnapshot): MessageStatus =
        MessageStatusRules.fromReceipts(
            deliveredAt = document.getLong(MessageSender.DELIVERED_AT),
            readAt = document.getLong(MessageSender.READ_AT)
        )

    /** Tells the sender this phone has the message (once). Firestore queues it while offline. */
    private fun reportDelivered(document: DocumentSnapshot) {
        if (document.getLong(MessageSender.DELIVERED_AT) != null) return
        document.reference.update(MessageSender.DELIVERED_AT, System.currentTimeMillis())
    }

    companion object {
        /** Per-user conversation choices live on the conversation document as lists of user ids. */
        const val PINNED_BY = "pinnedBy"
        const val MUTED_BY = "mutedBy"
        const val ARCHIVED_BY = "archivedBy"
    }
}
