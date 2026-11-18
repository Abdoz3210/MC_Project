package com.lafamilia.app

import android.content.Context
import com.lafamilia.app.data.InMemoryChatRepository
import com.lafamilia.app.data.auth.AuthRepository
import com.lafamilia.app.domain.ChatRepository

/**
 * A tiny hand-written "service locator". Screens ask it for repositories instead of
 * creating them, so we can swap the implementation without touching any screen.
 *
 * Part 1 ships an in-memory chat repository so the app already runs end to end.
 * Part 4 replaces this file and plugs in the Room + Firestore implementation.
 */
object AppGraph {

    val authRepository: AuthRepository by lazy { AuthRepository() }

    lateinit var chatRepository: ChatRepository
        private set

    fun init(@Suppress("UNUSED_PARAMETER") context: Context) {
        chatRepository = InMemoryChatRepository()
    }
}
