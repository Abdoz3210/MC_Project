# Part 2 - Conversation list (owner: Member 2)

## What it does
The home screen: all conversations, newest first, with search, unread badges, a "new chat" dialog
that searches users by name, and long-press to remove a conversation.

## Files
- `ui/chatlist/ConversationFilter.kt` - pure search function (title or last message, ignoring case).
- `util/TimeFormat.kt` - "14:05" today, "17 Nov" this year, "17 Nov 2025" older; clock/zone/locale are parameters so it is testable.
- `ui/chatlist/ChatListUiState.kt` - one immutable object for the whole screen, including dialogs.
- `ui/chatlist/ChatListViewModel.kt` - collects `observeConversations()`, handles search, debounced user search, new chat, delete.
- `ui/chatlist/ChatListComponents.kt` - row, avatar, badge, empty state, dialogs.
- `ui/chatlist/ChatListScreen.kt` - Scaffold, top bar, search field, `LazyColumn`, FAB.
- tests: `ConversationFilterTest`, `TimeFormatTest`.

## Ideas to be able to explain
- **LazyColumn + `key = { it.id }`**: only visible rows are composed; keys keep identity when the list changes.
- **Debounce** in `onUserQueryChange`: cancel the previous `Job`, wait 300 ms, then search - avoids a server query per keystroke.
- **`openChatId` one-shot event inside state**: ViewModel asks to navigate, screen navigates once and calls `onChatOpened()`.
- **`visibleConversations` as a computed property**: derived data is never stored twice.
- **`combinedClickable`**: tap and long-press on the same row.

## Likely questions
- Why is filtering in its own object? (testable, no UI)
- What does `collectAsStateWithLifecycle` do compared with `collectAsState`? (stops collecting when the app is in background)
- What if the search request fails? (`failed = true` shows an error text; the `CancellationException` is re-thrown on purpose)
- How would you add "pin conversation"? (a field in the model + sort rule in the repository)
