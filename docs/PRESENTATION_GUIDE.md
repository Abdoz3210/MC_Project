# Presentation and reflection guide

The doctor asks random questions about YOUR part, so everyone must be able to answer for it.
Read your file in `docs/parts/` the night before and practise the questions at the bottom.

## 5-minute demo order
1. Part 1 - register, log in, profile, sign out
2. Part 2 - chat list, search, new chat
3. Part 3 - send, edit, delete a message
4. Part 4 - airplane-mode demo: send offline, reconnect, message goes through
5. Part 5 - dark theme and Arabic (RTL) switch
6. Part 6 - invite friends share sheet, feedback email, unit test run

## Questions anyone may get (answers in the part files)
- Why is Room the source of truth instead of reading Firestore directly?
- What does a ViewModel give us that a composable does not?
- Why `val` in data classes, and why `copy()` instead of changing fields?
- What is state hoisting? Show an example in your part.
- What happens to a message sent while offline?
- Why do the screens depend on an interface (`ChatRepository`)?
- Which parts of the code did AI help with, and what did you check yourself?

## Honesty note
The code was generated with AI assistance (the doctor allows it). The commits carry a
Co-Authored-By trailer to say so. Your job is to understand your part and be able to change it live.
