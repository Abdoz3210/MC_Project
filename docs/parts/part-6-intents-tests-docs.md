# Part 6 - Intents, tests, QA, report material (owner: Member 6)

## What it does
Implicit Intents (invite friends via the share sheet, send feedback by email), the test suites
for the pure logic, the QA checklist, the architecture diagram, the abstract draft and the presentation guide.

## Files
- `util/InviteText.kt` - pure text builders.
- `util/IntentHelper.kt` - `shareText` (ACTION_SEND + chooser), `sendEmail` (ACTION_SENDTO `mailto:`), `findActivity`.
- `ui/invite/InviteSection.kt` - the two buttons (shown inside Settings).
- tests: `InviteTextTest`, `AuthValidatorTest`, `InMemoryChatRepositoryTest`.
- docs: `QA_CHECKLIST.md`, `diagrams/architecture.md`, `abstract/abstract.md`, `PRESENTATION_GUIDE.md`.

## Ideas to be able to explain
- **Implicit vs explicit Intent**: implicit says what we want; Android picks the app. Explicit names the class.
- **Why `createChooser`?** Always shows the picker instead of a saved default.
- **Why `mailto:` data?** Restricts the choice to email apps.
- **`ActivityNotFoundException`**: handled, we show a Toast instead of crashing.
- **Why test the in-memory repository?** It is the contract all screens rely on; the same tests describe the behaviour Part 4 must keep.

## Likely questions
- Show the share sheet. Where does the text come from?
- How do you run the tests? (`./gradlew testDebugUnitTest`)
- What is the difference between a unit test and an instrumented test?
- Change the invitation text live and show it.
