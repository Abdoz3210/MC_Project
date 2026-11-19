# La Familia: an offline-first family chat app for Android

**Course:** Mobile Computing (FNI 421) - Phase 1
**Team 43:** Rana Ahmed ElAhmedy, Abdelrhman Salah Ahmed, Alaa Ahmed Yagoub Eissa,
Abdelrhman Abdelrahman Hosnie Abdelradi, Youssef Emad, Muhammed Kamel

> The formatted 2-page ACM version is [`abstract.tex`](abstract.tex) (PDF: [`../abstract.pdf`](../abstract.pdf)).
> This Markdown file is the earlier plain-text draft.

## Abstract

Messaging apps assume a stable internet connection, yet many families live with slow or interrupted mobile data. La Familia is an Android chat application designed so that a conversation never depends on the connection being available at the moment a message is written. The app is written in Kotlin with Jetpack Compose and follows a single-activity, unidirectional-data-flow architecture in which each screen is driven by a ViewModel exposing an immutable UI state.

Persistence is the core of the design. A Room database is the single source of truth for conversations and messages, so the interface always renders local data and keeps working offline. Sending a message first stores it locally with the status QUEUED, then hands it to WorkManager, which delivers it to Firebase Firestore as soon as a network is available and retries with exponential back-off; after repeated failures the message is marked FAILED and can be retried by tapping it. A Firestore snapshot listener copies remote changes (new, edited and deleted messages) back into Room, which makes the list update reactively through Kotlin Flows. User preferences (theme, language, notifications) are stored with Preferences DataStore with full create, read, update and delete operations.

Account management uses Firebase Authentication with email and password, input validation and a password-strength indicator, and a public profile document that other users can search by name to start a conversation. The interface supports English and Arabic, including automatic right-to-left mirroring, and light and dark themes that can be changed at runtime without restarting. Implicit Intents are used to share an invitation through the system share sheet and to open the user's email application with a pre-filled feedback message.

Business rules such as input validation, message permissions, time formatting and conversation filtering are implemented as pure Kotlin functions and covered by unit tests, and the repository contract is implemented twice (an in-memory version for tests and a Room and Firestore version for production), which allowed six team members to build screens in parallel against a shared interface.

## Keywords
Jetpack Compose, Room, WorkManager, Firebase, offline-first, DataStore, Android Intents

## Main features
| Area | What it demonstrates |
|---|---|
| Compose UI | Material 3, navigation, dialogs, lists, state hoisting |
| Intents | Share sheet (ACTION_SEND) and email (ACTION_SENDTO) |
| Room | Entities, DAOs, Flow queries, source of truth |
| DataStore | Settings with create, read, update, delete |
| WorkManager | Reliable background delivery with constraints and back-off |
| Firebase | Authentication and Firestore real-time sync |
| Localisation | English and Arabic with RTL |

(The ACM version in `abstract.tex` supersedes this draft.)
