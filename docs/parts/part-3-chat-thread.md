# Part 3 - Chat thread (owner: Member 3)

## What it does
The conversation screen: message bubbles (mine on one side, theirs on the other), a composer,
edit and delete of your own messages, status text (Waiting / Sent / Failed) and tap-to-retry.

## Files
- `ui/chat/ChatRules.kt` - pure rules: trim and limit text, who may edit/delete, who may retry.
- `ui/chat/ChatUiState.kt`, `ChatViewModel.kt` - state and logic; uses a **factory** to receive `conversationId`.
- `ui/chat/ChatComponents.kt` - bubble, action dialog, delete confirmation.
- `ui/chat/ChatScreen.kt` - top bar, `LazyColumn`, composer with edit mode, auto-scroll.
- test: `ChatRulesTest`.

## Ideas to be able to explain
- **CRUD**: Create = send, Read = observe, Update = edit, Delete = delete (all through `ChatRepository`).
- **Edit mode** is just state: `editing != null` changes the composer and the send button's meaning.
- **ViewModel factory**: a ViewModel with constructor arguments cannot be created by default; `viewModelFactory { initializer { ... } }` builds it, `key = "chat-$id"` keeps one per conversation.
- **Auto scroll**: `LaunchedEffect(messages.size)` runs when the size changes.
- **Reading marks as read**: each time the list emits while the chat is open, `markConversationRead` runs.
- **RTL**: bubbles use `Arrangement.Start/End`, which flip automatically in Arabic.

## Likely questions
- Why can't you edit someone else's message? (`ChatRules.canModify`, also enforced by Firestore rules in Part 4)
- What happens when you send with no internet? (Part 4 queues it; here you only see "Waiting")
- Why `imePadding()`? (keeps the composer above the keyboard)
