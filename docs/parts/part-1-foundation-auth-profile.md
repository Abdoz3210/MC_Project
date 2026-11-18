# Part 1 - Foundation, authentication, profile (owner: Abdos)

## What it does
Gives the whole team a running skeleton: every screen has a route, every part of the data layer
has an interface, and login / register / profile are finished.

## Files
- `domain/Models.kt` - plain data classes (`User`, `Conversation`, `Message`) and `MessageStatus`.
- `domain/ChatRepository.kt` - the contract all screens use. Parts 2-3 call it; Part 4 implements it.
- `data/InMemoryChatRepository.kt` - a fake that keeps data in memory. Lets the app run before Part 4 exists and powers the unit tests.
- `AppGraph.kt` - creates shared objects once; screens ask it for the repository.
- `LaFamiliaApp.kt` - `Application` class; builds the graph at process start.
- `domain/AuthValidator.kt` - pure validation (email, password length, match, strength). No Android classes, so it is unit tested.
- `data/auth/AuthRepository.kt` - Firebase Auth + a `users/{uid}` profile document.
- `util/TaskExt.kt` - `Task<T>.await()`: turns Firebase callbacks into suspend functions.
- `ui/auth/*` - Login and Register: UiState, ViewModel, Screen, shared components.
- `ui/profile/*` - edit name and status, sign out.
- `navigation/AppNavHost.kt` - routes + start destination chosen from the saved session.
- `ui/chatlist|chat|settings|invite` placeholder screens that Parts 2, 3, 5, 6 replace.

## How it works in 6 sentences
1. `MainActivity` shows `AppRoot`, which shows the `NavHost`.
2. At start, `AppNavHost` checks `isSignedIn`; logged-in users skip Login.
3. `LoginScreen` is *stateless content* + a thin *stateful wrapper* (state hoisting).
4. The `ViewModel` holds a `MutableStateFlow<UiState>`; the screen collects it with `collectAsStateWithLifecycle`.
5. The ViewModel validates with `AuthValidator`, then calls `AuthRepository` inside `viewModelScope.launch`.
6. On success it sets `isLoggedIn = true`; the screen reacts in a `LaunchedEffect` and navigates, popping Login off the back stack.

## Likely questions
- **Why a ViewModel?** It survives rotation and holds state outside the composable.
- **Why `val` and `copy()`?** Immutable state is easier to reason about; Compose detects change by new object.
- **What is `popUpTo ... inclusive`?** Removes Login from the back stack so Back doesn't return to it.
- **Why is `AuthValidator` an `object` with no Android imports?** So it runs as a plain JVM unit test.
- **Why `runCatching` in `AuthRepository`?** Firebase throws exceptions; we turn them into `Result` so the ViewModel never crashes.
- **Why `await()`?** Callbacks are hard to chain; a suspend function reads top to bottom.
