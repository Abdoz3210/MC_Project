# Data model

Firestore is shared in the cloud. Room is the local copy that the UI reads.

```mermaid
erDiagram
    USER ||--o{ MESSAGE : sends
    USER ||--o{ CONVERSATION_MEMBER : "is in"
    CONVERSATION ||--o{ CONVERSATION_MEMBER : has
    CONVERSATION ||--o{ MESSAGE : contains

    USER {
        string id PK
        string name
        string username
        string avatarUrl
        string status
    }
    CONVERSATION {
        string id PK
        string type
        string title
        string lastMessageText
        long lastMessageAt
    }
    CONVERSATION_MEMBER {
        string conversationId PK, FK
        string userId PK, FK
    }
    MESSAGE {
        string id PK
        string conversationId FK
        string senderId FK
        string body
        string status
        long createdAt
    }
```

## Firestore structure
- `users/{uid}`
- `conversations/{conversationId}`: direct chats use the sorted user IDs joined by `_`; groups use a random ID
- `conversations/{conversationId}/messages/{messageId}`: the ID is created on the phone (retry-safe)

## Rules
- The UI reads only from Room; the repository syncs Firestore into Room.
- Message status: QUEUED, SENDING, SENT, DELIVERED, READ, FAILED.