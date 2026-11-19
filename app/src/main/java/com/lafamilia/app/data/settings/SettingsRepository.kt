package com.lafamilia.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/** One DataStore file for the whole app. The delegate guarantees a single instance. */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Persistence with DataStore (key-value, asynchronous, safe on any thread).
 * CRUD: Create/Update = the set... functions, Read = [settings], Delete = [reset].
 */
class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    val settings: Flow<AppSettings> = dataStore.data
        .catch { error ->
            // A corrupted file must not crash the app: fall back to defaults.
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs ->
            AppSettings(
                theme = ThemeMode.fromName(prefs[THEME]),
                language = AppLanguage.fromName(prefs[LANGUAGE]),
                notificationsEnabled = prefs[NOTIFICATIONS] ?: true
            )
        }

    suspend fun setTheme(mode: ThemeMode) {
        dataStore.edit { it[THEME] = mode.name }
    }

    suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { it[LANGUAGE] = language.name }
    }

    suspend fun setNotifications(enabled: Boolean) {
        dataStore.edit { it[NOTIFICATIONS] = enabled }
    }

    /** Delete: forgets every saved choice, so the defaults apply again. */
    suspend fun reset() {
        dataStore.edit { it.clear() }
    }

    companion object {
        private val THEME = stringPreferencesKey("theme")
        private val LANGUAGE = stringPreferencesKey("language")
        private val NOTIFICATIONS = booleanPreferencesKey("notifications")

        fun from(context: Context): SettingsRepository =
            SettingsRepository(context.applicationContext.settingsDataStore)
    }
}
