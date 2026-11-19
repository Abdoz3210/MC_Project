# Architecture

```mermaid
flowchart TD
    UI["Compose screens<br/>(Login, Register, ChatList, Chat, Profile, Settings)"]
    VM["ViewModels<br/>StateFlow of UiState"]
    REPO["ChatRepository (interface)"]
    ROOM["RoomChatRepository"]
    DB[("Room database<br/>source of truth")]
    SYNC["FirestoreSync<br/>server -> Room"]
    WORK["WorkManager<br/>SendMessageWorker"]
    FS[("Firebase Firestore")]
    AUTH["AuthRepository<br/>Firebase Auth"]
    DS[("DataStore<br/>settings")]

    UI --> VM --> REPO
    REPO -.implemented by.-> ROOM
    ROOM --> DB
    FS --> SYNC --> DB
    ROOM -->|queue message| WORK -->|when online| FS
    VM --> AUTH
    UI --> DS
```

## Message life cycle

```mermaid
stateDiagram-v2
    [*] --> QUEUED: user taps send (saved in Room)
    QUEUED --> SENDING: worker starts (internet available)
    SENDING --> SENT: Firestore accepted it
    SENDING --> QUEUED: network error, retry later
    QUEUED --> FAILED: 5 failed attempts
    FAILED --> QUEUED: user taps the message
```

## Rules the code follows
1. Screens only know `ChatRepository`, never Room or Firestore directly.
2. Room is the single source of truth; the UI only reads Room.
3. Every write goes to Room first, then to the server in the background.
4. State flows down (ViewModel -> screen), events flow up (screen -> ViewModel).
