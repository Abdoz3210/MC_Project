# Part 5 - Settings, DataStore, theme, language (owner: Member 5)

## What it does
Settings screen with theme (system/light/dark), language (system/English/Arabic with RTL),
notifications switch and "reset". Everything is saved with DataStore and applied instantly.

## Files
- `data/settings/AppSettings.kt` - enums + immutable settings object; `fromName` fallbacks.
- `data/settings/SettingsRepository.kt` - DataStore Preferences: set (create/update), `settings` flow (read), `reset` (delete).
- `ui/settings/SettingsViewModel.kt`, `SettingsScreen.kt` - radio rows, switch, reset dialog, includes Part 6's `InviteSection`.
- `ui/app/AppRoot.kt` - reads settings and wraps the app in locale + theme.
- `ui/app/LocaleWrapper.kt` - provides a Context with the chosen language and `LayoutDirection.Rtl` for Arabic.
- `ui/theme/Color.kt`, `Theme.kt` - brand palette, light/dark schemes.
- `values-ar/strings_common.xml` - Arabic app name.
- test: `AppSettingsTest`.

## Ideas to be able to explain
- **DataStore vs SharedPreferences**: asynchronous, Flow-based, no main-thread blocking, transactional.
- **Preferences keys**: typed keys (`stringPreferencesKey`) - we store enum *names* as text.
- **`stateIn(WhileSubscribed(5000))`**: turns the cold Flow into a StateFlow that stops 5 s after the UI leaves.
- **Locale without recreating the Activity**: our `LocaleContext` overrides `getResources()`, and `LocalLayoutDirection` flips layout.
- **Why `AndroidViewModel`?** DataStore needs a Context.

## Likely questions
- Where is the file stored? (app-private `datastore/settings.preferences_pb`)
- What happens if the file is corrupt? (`catch` returns defaults)
- Why enum `fromName` with a default? (old/unknown values never crash)
- Notifications switch: only stored for now; real push notifications are future work.
