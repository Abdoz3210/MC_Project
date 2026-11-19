package com.lafamilia.app.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.lafamilia.app.util.await
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

data class ProfileData(val name: String, val email: String, val status: String)

/**
 * Everything about "who is the user": Firebase Authentication for the account,
 * and a small `users/{uid}` document in Firestore for the public profile
 * (name, searchable lowercase name, status) that other people can find.
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    val isSignedIn: Boolean get() = auth.currentUser != null
    val userId: String get() = auth.currentUser?.uid.orEmpty()
    val displayName: String get() = auth.currentUser?.displayName.orEmpty()

    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
        Unit
    }.onSuccess { ensureProfileDocument() }

    suspend fun register(name: String, email: String, password: String): Result<Unit> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = checkNotNull(result.user) { "Firebase returned no user" }
        val request = UserProfileChangeRequest.Builder().setDisplayName(name).build()
        user.updateProfile(request).await()
        // The account exists now. A failure to write the public profile must not make
        // registration look failed; login will retry it (ensureProfileDocument).
        try {
            writeProfileDocument(user.uid, name, email, DEFAULT_STATUS)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // ignored on purpose: retried by ensureProfileDocument() on the next login
        }
    }

    suspend fun loadProfile(): ProfileData {
        val user = auth.currentUser
        val document = runCatching {
            withTimeout(LOAD_TIMEOUT_MS) { firestore.collection(USERS).document(userId).get().await() }
        }.getOrNull()
        return ProfileData(
            name = user?.displayName.orEmpty(),
            email = user?.email.orEmpty(),
            status = document?.getString("status") ?: DEFAULT_STATUS
        )
    }

    suspend fun updateProfile(name: String, status: String): Result<Unit> = runCatching {
        val user = checkNotNull(auth.currentUser) { "Not signed in" }
        val request = UserProfileChangeRequest.Builder().setDisplayName(name).build()
        user.updateProfile(request).await()
        withTimeout(SAVE_TIMEOUT_MS) { writeProfileDocument(user.uid, name, user.email.orEmpty(), status) }
    }

    fun signOut() = auth.signOut()

    private suspend fun ensureProfileDocument() {
        runCatching {
            val user = auth.currentUser ?: return@runCatching
            val email = user.email.orEmpty()
            val name = user.displayName?.takeIf { it.isNotBlank() } ?: email.substringBefore('@')
            withTimeoutOrNull(SAVE_TIMEOUT_MS) { writeProfileDocument(user.uid, name, email, status = null) }
        }
    }

    private suspend fun writeProfileDocument(uid: String, name: String, email: String, status: String?) {
        val data = mutableMapOf<String, Any>(
            "name" to name,
            "nameLower" to name.lowercase(),
            "username" to email.substringBefore('@').lowercase()
        )
        if (status != null) data["status"] = status
        firestore.collection(USERS).document(uid).set(data, SetOptions.merge()).await()
    }

    companion object {
        const val DEFAULT_STATUS = "Hey there! I am using La Familia"
        private const val USERS = "users"
        private const val LOAD_TIMEOUT_MS = 5_000L
        private const val SAVE_TIMEOUT_MS = 10_000L
    }
}
