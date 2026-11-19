package com.lafamilia.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Objects: each function is one SQL statement. Room generates the code.
 * Functions returning Flow re-emit automatically whenever the table changes,
 * which is how the UI updates itself without any manual refresh.
 */
@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversations ORDER BY lastMessageAt DESC")
    fun observeAll(): Flow<List<ConversationEntity>>

    @Query("SELECT title FROM conversations WHERE id = :id")
    fun observeTitle(id: String): Flow<String?>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun get(id: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ConversationEntity)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :id")
    suspend fun markRead(id: String)

    @Query("UPDATE conversations SET unreadCount = unreadCount + :amount WHERE id = :id")
    suspend fun addUnread(id: String, amount: Int)

    @Query("UPDATE conversations SET lastMessageText = :text, lastMessageAt = :at WHERE id = :id")
    suspend fun updateLast(id: String, text: String, at: Long)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE conversations SET pinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: String, pinned: Boolean)

    @Query("UPDATE conversations SET muted = :muted WHERE id = :id")
    suspend fun setMuted(id: String, muted: Boolean)

    @Query("UPDATE conversations SET archived = :archived WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean)
}

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    fun observe(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun get(id: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MessageEntity)

    @Query("UPDATE messages SET status = :status WHERE id = :id")
    suspend fun setStatus(id: String, status: String)

    /** Marks a message SENT only if it was still waiting, so a faster DELIVERED/READ is not overwritten. */
    @Query("UPDATE messages SET status = 'SENT' WHERE id = :id AND status IN ('QUEUED', 'SENDING')")
    suspend fun markSentIfPending(id: String)

    /** Messages from the other person that this phone has not yet reported as read. */
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND senderId != :me AND status != 'READ'")
    suspend fun unreadIncoming(conversationId: String, me: String): List<MessageEntity>

    @Query("UPDATE messages SET body = :body WHERE id = :id")
    suspend fun setBody(id: String, body: String)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteForConversation(conversationId: String)
}
