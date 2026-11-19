# La Familia

[![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Language](https://img.shields.io/badge/language-Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Backend](https://img.shields.io/badge/backend-Firebase-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com)
[![Min SDK](https://img.shields.io/badge/minSdk-26-blue)](#getting-started)
[![Status](https://img.shields.io/badge/status-in%20development-orange)](#roadmap)

**La Familia** is an Android messaging app for private conversations. Users create
an account, find other users, and exchange text messages in real time. Messages are
stored locally with Room so conversations stay available offline, and messages sent
without a connection are queued and delivered automatically once the device is back
online. The interface supports English and Arabic, including right-to-left layouts.

The app is the Phase 1 project of the **Mobile Computing (FNI-421)** course. Its
design is inspired by the Pulse Chat web application.

> **Course:** Mobile Computing (FNI-421), Phase 1  
> **Team number:** 43

---

## Table of contents

- [Features](#features)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Project structure](#project-structure)
- [Getting started](#getting-started)
- [Team](#team)
- [Development workflow](#development-workflow)
- [Roadmap](#roadmap)
- [Documentation](#documentation)

---

## Features

| Area | Feature |
|---|---|
| Accounts | Register, sign in, sign out, persistent session (Firebase Authentication) |
| Conversations | Conversation list with last message, time, and unread count |
| Messaging | Send and receive text messages with status: sent, delivered, read |
| Offline support | Local storage with Room; unsent messages are queued and retried with backoff |
| Sharing | Share an invite link with an Android Intent |
| Settings | Language (English / Arabic with RTL), light and dark theme |
| Profile | Display name and presence status |

Planned if time allows: group chats, typing indicators, push notifications, and
pin / mute / archive for conversations.

**Out of scope:** voice and video calls, file and image sharing, end-to-end encryption.

## Tech stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Navigation | Navigation Compose |
| State | ViewModel, StateFlow |
| Local storage | Room (messages and conversations), DataStore (settings) |
| Cloud | Firebase Authentication, Cloud Firestore |
| Background work | WorkManager (offline queue retry) |
| Build | Gradle (Kotlin DSL), KSP |

## Architecture

The app follows **MVVM** with a repository layer. **Room is the single source of
truth:** screens only read from the local database, and the repository keeps it in
sync with Cloud Firestore.

```
 Compose UI  -->  ViewModel  -->  Repository  -->  Room (local, source of truth)
   (screens)     (UI state)           |
                                      +----------->  Cloud Firestore / Firebase Auth
```

- **UI layer:** stateless composables that render state from a ViewModel.
- **ViewModel layer:** exposes UI state as `StateFlow` and handles user actions.
- **Data layer:** repositories hide where data comes from. Firestore listeners write
  into Room, and the UI observes Room through `Flow`.
- **Offline queue:** an unsent message is saved in Room with a pending status and
  retried by WorkManager with exponential backoff. A client-generated message ID
  prevents duplicates on retry.

## Project structure

```
MC_Project/
├── app/                  Android application module
│   └── src/main/
│       ├── java/...      Kotlin source (ui, viewmodel, data, util)
│       └── res/          Resources (strings in en and ar, themes, icons)
├── docs/                 Wireframes, ER diagram, ACM abstract
├── gradle/               Gradle wrapper and version catalog
└── README.md
```

## Getting started

### Prerequisites

- Android Studio (current stable release)
- JDK 17 or newer (bundled with Android Studio)
- An Android emulator or a physical device running Android 8.0 (API 26) or newer
- Access to the team's Firebase project

### Run the app

1. Clone the repository:
   ```bash
   git clone https://github.com/Abdoz3210/MC_Project.git
   cd MC_Project
   ```
2. Open the folder in Android Studio and wait for the Gradle sync to finish.
3. Add the Firebase configuration file (see below).
4. Select a device and press **Run**.

### Firebase configuration

The app needs a `google-services.json` file for the team's Firebase project.

1. In the Firebase console, open **Project settings** and download
   `google-services.json` for the Android app (`com.lafamilia.app`).
2. Place it in the `app/` directory.
3. The file is listed in `.gitignore` and must not be committed.

Ask the repository owner to add you to the Firebase project if you do not have access.

## Team

| # | Member | Area of ownership |
|---|---|---|
| 1 | [Abdoz3210](https://github.com/Abdoz3210) | Authentication and profile, project foundation |
| 2 | [abdohosnie](https://github.com/abdohosnie) | Conversation list and search |
| 3 | [Alaa-AhmedY](https://github.com/Alaa-AhmedY) | Chat thread and message composer |
| 4 | [MKamel87](https://github.com/MKamel87) | Room database and Firestore sync |
| 5 | [rana337](https://github.com/rana337) | Settings, localization (EN / AR), theme |
| 6 | [youssefemad-02](https://github.com/youssefemad-02) | Intent sharing, documentation, QA |

## Development workflow

All work is tracked as [GitHub Issues](../../issues), grouped into
[milestones](../../milestones) that match the course grading criteria.

**Branches**

- `main` always builds and runs. Nobody commits to it directly.
- Each member has a personal branch `dev/<github-username>`.
- Feature work uses `feature/<issue-number>-<short-name>`.

**Commits** follow [Conventional Commits](https://www.conventionalcommits.org):

```
feat: add chat list screen
fix: prevent duplicate message on retry
docs: update architecture section
refactor: replace var with val in MessageMapper
test: add unit tests for offline queue
```

**Pull requests**

1. Open a pull request into `main` that references its issue with `Closes #<number>`.
2. At least one teammate reviews it.
3. The app must build before merging.
4. Merging closes the issue automatically.

**Code style**

- Prefer `val` over `var`, and use lambdas, data classes, and scope functions where
  they make code clearer.
- Keep business logic out of composables. Use a ViewModel.
- Use theme values and string resources instead of hard-coded colors and text.

## Roadmap

| Milestone | Focus | Grading criterion |
|---|---|---|
| M1 Setup and planning | Repo, scope, Firebase project, wireframes | n/a |
| M2 Core features | Compose screens, navigation, Intent | Feature coverage |
| M3 Persistence | Room CRUD, Firestore sync, offline queue | Persistence data |
| M4 UI/UX polish | Responsive layouts, RTL, dark theme, accessibility | UI/UX quality |
| M5 Code quality and abstract | Kotlin cleanup, tests, 2-page ACM abstract | Code quality, abstract |
| M6 Final review | Clean build, final README, presentation practice | Contribution, reflection |

## Documentation

- Wireframes and ER diagram: [`docs/`](docs/)
- 2-page abstract (ACM format): [`docs/abstract.pdf`](docs/abstract.pdf), source [`docs/abstract/abstract.tex`](docs/abstract/abstract.tex)

---

_Developed as coursework for Mobile Computing (FNI-421)._