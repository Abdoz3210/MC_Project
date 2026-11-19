# QA checklist (run before the demo)

Tick each line on a real emulator or phone. Write the date and who tested.

## Account
- [ ] Register with a valid email, name and 6+ character password -> lands on chat list
- [ ] Register with a short password -> error message, no crash
- [ ] Register with mismatched passwords -> error message
- [ ] Password strength bar changes Weak / Medium / Strong while typing
- [ ] Log out (Profile) -> back to Login; back button does not return into the app
- [ ] Log in with a wrong password -> readable error
- [ ] Kill the app and reopen while logged in -> goes straight to chat list

## Chats
- [ ] Search finds a conversation by title and by last message
- [ ] "+" -> search a registered user -> chat opens
- [ ] Send a message -> appears at once, status changes Waiting -> Sent
- [ ] Long-press own message -> Edit works; Delete asks for confirmation
- [ ] Long-press the other person's message -> no menu
- [ ] Unread badge appears for new messages and clears after opening the chat
- [ ] Long-press a conversation -> Remove -> disappears

## Offline (the important one)
- [ ] Turn on airplane mode, send a message -> shows Waiting
- [ ] Turn airplane mode off -> message becomes Sent without opening anything
- [ ] Force-stop the app while a message waits, then reopen online -> it still gets sent
- [ ] Open the app offline -> old chats and messages are still visible

## Settings and Intents
- [ ] Theme: Light / Dark / System each apply immediately
- [ ] Language: Arabic flips the whole layout to right-to-left; English flips back
- [ ] Settings survive killing the app
- [ ] Reset settings returns to defaults
- [ ] Invite friends opens the Android share sheet with the invitation text
- [ ] Send feedback opens an email app with subject and body filled

## Quality
- [ ] Rotate the screen on every screen -> nothing is lost
- [ ] `./gradlew testDebugUnitTest` passes
- [ ] No red text in Logcat when using the app normally
