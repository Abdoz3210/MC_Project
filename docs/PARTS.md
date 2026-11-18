# Who owns which part

Merge order: **Part 1 first** (it is the foundation), then Parts 2-6 in any order.
Each member runs their own `apply_part_N.sh` on their own computer, logged in to GitHub as themselves.

| Part | Owner | What it is | Rubric item it covers | Difficulty |
|---|---|---|---|---|
| 1 | Abdos (leader) | Foundation: navigation, models, repository contract, login, register, profile, Gradle setup | Compose, UI/UX, code quality | Medium (biggest) |
| 2 | Member 2 | Conversation list: search, unread badges, new chat dialog, time formatting | Compose, lists, UI/UX | Easy |
| 3 | Member 3 | Chat thread: bubbles, composer, edit, delete, retry | Compose, CRUD in UI | Medium |
| 4 | Member 4 (strongest in the team) | Room database, Firestore sync, WorkManager sending, security rules | Persistence (Room) | Hard |
| 5 | Member 5 | Settings with DataStore, theme, Arabic/English with RTL | Persistence (DataStore), UI/UX | Medium |
| 6 | Member 6 | Intents (share, email), unit tests, QA checklist, abstract, diagrams, presentation guide | Intents, code quality, abstract | Easy-Medium |

Fill in the real names and GitHub usernames in your copy of this file.

Every part ships with `docs/parts/part-N-*.md`: what the code does, file by file, and the
questions the doctor is likely to ask. Read it, run the app, change something small yourself.
