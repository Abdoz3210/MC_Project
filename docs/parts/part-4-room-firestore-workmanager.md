# Part 4 - Room, Firestore sync, WorkManager (owner: Member 4)

## What it does
Replaces the in-memory fake with the real data layer. Offline-first: the UI reads only Room;
messages are saved locally first and delivered in the background.

## Files
- `data/local/Entities.kt` - tables `conversations` and `messages`.
- `data/local/Daos.kt` - SQL as functions; `Flow` return types re-emit when the table changes.
- `data/local/AppDatabase.kt`, `Mappers.kt` - database class; entity <-> model conversion.
- `data/remote/FirestoreSync.kt` - snapshot listeners copy server changes into Room.
- `data/remote/MessageSender.kt` - sends one queued message (batch write, idempotent).
- `data/work/SendMessageWorker.kt` - WorkManager worker; runs when online, retries with back-off, gives up after 5 tries.
- `data/RoomChatRepository.kt` - implements `ChatRepository`.
- `AppGraph.kt` - now builds Room, Firestore, WorkManager.
- `docs/firestore.rules` - **paste into Firebase console -> Firestore -> Rules -> Publish**, otherwise reads/writes are denied.
- test: `MappersTest`.

## Message life cycle
`sendMessage` -> insert QUEUED in Room (UI shows it instantly) -> enqueue worker with `NetworkType.CONNECTED`
-> worker sets SENDING, writes to Firestore -> SENT. Firestore listener later sees the document and keeps it SENT.
Network error -> back to QUEUED, WorkManager retries (10 s, 20 s, 40 s ...). 5 failures -> FAILED, user taps to retry.

## Ideas to be able to explain
- **Single source of truth**: UI <- Room <- (sync) <- Firestore. Never UI <- Firestore.
- **Idempotent write**: message id is generated on the phone and used as the document id; retrying cannot duplicate.
- **Why WorkManager, not a coroutine?** It survives app close and process death and respects constraints.
- **Why wipe Room when another user logs in?** So users never see someone else's chats (`ensureSync`).
- **Unread counter**: incremented in the sync when a message from someone else is *new* to Room.
- **Leaving a chat** removes your uid from `members`; your query stops matching, sync removes it locally.

## Likely questions
- What if the phone is offline when I tap send? What if I close the app?
- Why `OnConflictStrategy.REPLACE`?
- What is `exportSchema = false`? (no schema JSON written; fine for a course project)
- Which Firestore rule stops me editing a stranger's message?
