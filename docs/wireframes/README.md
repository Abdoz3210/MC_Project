# Wireframes

Wireframes of every screen in La Familia, drawn from the labels, components and
layout used in the app (`app/src/main/java/com/lafamilia/app/ui/`).

Colors are the app's own Material 3 theme (`ui/theme/Color.kt`, `ui/theme/Theme.kt`):
terracotta primary (`#B4492F` light / `#FFB4A1` dark), its container tone for your own
message bubbles, avatars and the + button, and the warm background (`#FFF8F6` light /
`#1A110F` dark). Roles the theme does not override use the Material 3 defaults.

## Navigation graph

![Navigation graph](navigation.svg)

The same graph as text (it matches `navigation/AppNavHost.kt`):

```mermaid
flowchart LR
    start([App start]) -->|not signed in| login[Login]
    start -->|signed in| chatlist[Chat list]
    login -->|Create account| register[Register]
    register -->|back| login
    login -->|login ok, clears back stack| chatlist
    register -->|registered| chatlist
    chatlist -->|open chat| chat["Chat thread<br/>chat/{conversationId}"]
    chatlist -->|profile icon| profile[Profile]
    chatlist -->|settings icon| settings[Settings]
    profile -->|sign out, clears back stack| login
```

Back from Chat thread, Profile and Settings returns to the Chat list (`popBackStack`).

## Screens

| | | |
|---|---|---|
| ![Login](01-login.svg) **1. Login**: email, password with show/hide, link to Register | ![Register](02-register.svg) **2. Register**: name, email, password with strength hint, confirm password | ![Chat list](03-chat-list.svg) **3. Chat list**: search, conversations with last message, time and unread badge, + for a new chat |
| ![New chat](04-new-chat.svg) **4. New chat**: debounced search for people by name | ![Chat thread](05-chat-thread.svg) **5. Chat thread**: bubbles with status, retry on failure, edit and delete own messages | ![Profile](06-profile.svg) **6. Profile**: display name, status, save, sign out |
| ![Settings](07-settings.svg) **7. Settings**: theme, language (Arabic switches to right-to-left), notifications, reset, invite and feedback Intents | | |

## Dark theme

The same screens with the dark theme (Settings → Theme → Dark, or "Follow system"
when the phone is in dark mode). Layout is identical; the colors switch to the
dark palette from `Color.kt`.

| | | |
|---|---|---|
| ![Login, dark](01-login-dark.svg) **1. Login** | ![Register, dark](02-register-dark.svg) **2. Register** | ![Chat list, dark](03-chat-list-dark.svg) **3. Chat list** |
| ![New chat, dark](04-new-chat-dark.svg) **4. New chat** | ![Chat thread, dark](05-chat-thread-dark.svg) **5. Chat thread** | ![Profile, dark](06-profile-dark.svg) **6. Profile** |
| ![Settings, dark](07-settings-dark.svg) **7. Settings** | | |

The SVGs are generated, so they stay easy to update: edit the text or layout and
open the file in any browser to check it.
