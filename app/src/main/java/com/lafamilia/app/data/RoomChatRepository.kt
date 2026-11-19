package com.lafamilia.app.data

import android.content.SharedPreferences
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.lafamilia.app.data.auth.AuthRepository
import com.lafamilia.app.data.local.AppDatabase
import com.lafamilia.app.data.local.ConversationEntity
import com.lafamilia.app.data.local.MessageEntity
import com.lafamilia.app.data.local.toDomain
import com.lafamilia.app.data.remote.FirestoreSync
import com.lafamilia.app.data.remote.MessageSender
import com.lafamilia.app.data.work.SendMessageWorker
import com.lafamilia.app.domain.ChatRepository
import com.lafamilia.app.domain.Conversation
import com.lafamilia.app.domain.Message
import com.lafamilia.app.domain.MessageStatus
import com.lafamilia.app.domain.User
import com.lafamilia.app.util.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * The real repository. Room is the single source of truth: every read comes from Room,
 * every write goes to Room first, and Firestore is only the sync channel.
 * That is why the app works offline and why sending is "queue, then deliver".
 */
class RoomChatRepository(
    private val database: AppDatabase,
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val sync: FirestoreSync,
    private val workManager: WorkManager,
    private val prefs: SharedPreferences,
    private val scope: CoroutineScope
) : ChatRepository {

    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()

    private var syncedUserId: String? = null

    override val currentUserId: String get() = authRepository.userId

    override fun observeConversations(): Flow<List<Conversation>> =
        conversationDao.observeAll()
            .map { rows -> rows.map { it.toDomain() } }
            .onStart { ensureSync() }

    override fun observeMessages(conversationId: String): Flow<List<Message>> =
        messageDao.observe(conversationId).map { rows -> rows.map { it.toDomain() } }

    override fun observeConversationTitle(conversationId: String): Flow<String> =
        conversationDao.observeTitle(conversationId).map { it.orEmpty() }

    override suspend fun searchUsers(query: String): List<User> {
        val text = query.trim().lowercase()
        if (text.isEmpty()) return emptyList()
        val snapshot = withTimeout(SEARCH_TIMEOUT_MS) {
            firestore.collection("users")
                .orderBy("nameLower")
                .startAt(text)
                .endAt(text + "")
                .limit(20)
                .get()
                .await()
        }
        return snapshot.documents
            .filter { it.id != currentUserId }
            .map { document ->
                User(
                    id = document.id,
                    name = document.getString("name").orEmpty(),
                    username = document.getString("username").orEmpty(),
                    status = document.getString("status").orEmpty()
                )
            }
    }

    override suspend fun startDirectChat(other: User): String {
        val me = currentUserId
        val id = listOf(me, other.id).sorted().joinToString("_")
        val myName = authRepository.displayName.ifBlank { "Me" }

        // Local first, so the chat can open at once even without internet.
        if (conversationDao.get(id) == null) {
            conversationDao.upsert(ConversationEntity(id, other.name, "", 0L, 0))
        }
        firestore.collection(MessageSender.CONVERSATIONS).document(id).set(
            mapOf(
                "members" to listOf(me, other.id),
                "memberNames" to mapOf(me to myName, other.id to other.name)
            ),
            SetOptions.merge()
        )
        return id
    }

    override suspend fun sendMessage(conversationId: String, body: String) {
        val now = System.currentTimeMillis()
        val entity = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = currentUserId,
            body = body,
            status = MessageStatus.QUEUED.name,
            createdAt = now
        )
        messageDao.upsert(entity)
        conversationDao.updateLast(conversationId, body, now)
        enqueueDelivery(entity.id)
    }

    override suspend fun editMessage(message: Message, newBody: String) {
        messageDao.setBody(message.id, newBody)
        // Best effort: Firestore applies it as soon as the phone is online.
        firestore.collection(MessageSender.CONVERSATIONS).document(message.conversationId)
            .collection(MessageSender.MESSAGES).document(message.id)
            .update("body", newBody)
    }

    override suspend fun deleteMessage(message: Message) {
        messageDao.delete(message.id)
        firestore.collection(MessageSender.CONVERSATIONS).document(message.conversationId)
            .collection(MessageSender.MESSAGES).document(message.id)
            .delete()
    }

    override suspend fun retryMessage(message: Message) {
        messageDao.setStatus(message.id, MessageStatus.QUEUED.name)
        enqueueDelivery(message.id)
    }

    override suspend fun markConversationRead(conversationId: String) {
        conversationDao.markRead(conversationId)
        sendReadReceipts(conversationId)
    }

    /**
     * Read receipts (issue #45): writes `readAt` on the other person's messages this phone has
     * not reported yet, then remembers locally that they are READ so each is reported once.
     */
    private suspend fun sendReadReceipts(conversationId: String) {
        val me = currentUserId
        if (me.isEmpty()) return
        val unread = messageDao.unreadIncoming(conversationId, me)
        if (unread.isEmpty()) return
        val now = System.currentTimeMillis()
        val messages = firestore.collection(MessageSender.CONVERSATIONS).document(conversationId)
            .collection(MessageSender.MESSAGES)
        val batch = firestore.batch()
        unread.forEach { batch.update(messages.document(it.id), MessageSender.READ_AT, now) }
        batch.commit()
        unread.forEach { messageDao.setStatus(it.id, MessageStatus.READ.name) }
    }

    override suspend fun leaveConversation(conversationId: String) {
        messageDao.deleteForConversation(conversationId)
        conversationDao.delete(conversationId)
        firestore.collection(MessageSender.CONVERSATIONS).document(conversationId)
            .update("members", FieldValue.arrayRemove(currentUserId))
    }

    override suspend fun setPinned(conversationId: String, pinned: Boolean) {
        conversationDao.setPinned(conversationId, pinned)
        saveChoice(conversationId, FirestoreSync.PINNED_BY, pinned)
    }

    override suspend fun setMuted(conversationId: String, muted: Boolean) {
        conversationDao.setMuted(conversationId, muted)
        saveChoice(conversationId, FirestoreSync.MUTED_BY, muted)
    }

    override suspend fun setArchived(conversationId: String, archived: Boolean) {
        conversationDao.setArchived(conversationId, archived)
        saveChoice(conversationId, FirestoreSync.ARCHIVED_BY, archived)
    }

    /**
     * Room is updated first so the list changes at once; Firestore keeps the choice per user
     * (the user's id is added to or removed from a list on the conversation), so it survives a
     * reinstall and follows the user to another phone. Firestore queues the write while offline.
     */
    private fun saveChoice(conversationId: String, field: String, enabled: Boolean) {
        val change = if (enabled) FieldValue.arrayUnion(currentUserId) else FieldValue.arrayRemove(currentUserId)
        firestore.collection(MessageSender.CONVERSATIONS).document(conversationId).update(field, change)
    }

    private fun enqueueDelivery(messageId: String) {
        val request = OneTimeWorkRequest.Builder(SendMessageWorker::class.java)
            .setInputData(Data.Builder().putString(SendMessageWorker.KEY_MESSAGE_ID, messageId).build())
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()
        workManager.enqueueUniqueWork("send-$messageId", ExistingWorkPolicy.REPLACE, request)
    }

    /**
     * Starts listening to Firestore for the signed-in user. If a different user used this phone
     * before, their local data is wiped first so nobody sees someone else's chats.
     */
    @Synchronized
    private fun ensureSync() {
        val uid = currentUserId
        if (uid == syncedUserId) return
        syncedUserId = uid
        sync.stop()
        scope.launch {
            val previousOwner = prefs.getString(KEY_OWNER, null)
            if (previousOwner != uid) {
                withContext(Dispatchers.IO) { database.clearAllTables() }
                prefs.edit().putString(KEY_OWNER, uid).apply()
            }
            if (uid.isNotEmpty()) sync.start(uid)
        }
    }

    private companion object {
        const val KEY_OWNER = "owner_uid"
        const val SEARCH_TIMEOUT_MS = 8_000L
    }
}
