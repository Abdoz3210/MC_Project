package com.lafamilia.app

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.google.firebase.firestore.FirebaseFirestore
import com.lafamilia.app.data.RoomChatRepository
import com.lafamilia.app.data.auth.AuthRepository
import com.lafamilia.app.data.local.AppDatabase
import com.lafamilia.app.data.remote.FirestoreSync
import com.lafamilia.app.data.remote.MessageSender
import com.lafamilia.app.domain.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Builds and holds the shared objects. Screens ask it for a repository and never
 * know whether it is the in-memory fake or the real Room + Firestore one.
 */
object AppGraph {

    val authRepository: AuthRepository by lazy { AuthRepository() }

    lateinit var database: AppDatabase
        private set
    lateinit var messageSender: MessageSender
        private set
    lateinit var chatRepository: ChatRepository
        private set

    fun init(context: Context) {
        val app = context.applicationContext
        val firestore = FirebaseFirestore.getInstance()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        database = Room.databaseBuilder(app, AppDatabase::class.java, "lafamilia.db").build()
        messageSender = MessageSender(database, firestore)
        chatRepository = RoomChatRepository(
            database = database,
            firestore = firestore,
            authRepository = authRepository,
            sync = FirestoreSync(database, firestore, scope),
            workManager = WorkManager.getInstance(app),
            prefs = app.getSharedPreferences("lafamilia_sync", Context.MODE_PRIVATE),
            scope = scope
        )
    }
}
