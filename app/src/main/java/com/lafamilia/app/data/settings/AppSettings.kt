package com.lafamilia.app.data.settings

enum class ThemeMode {
    SYSTEM, LIGHT, DARK;

    companion object {
        /** Unknown or missing text gives the default instead of crashing. */
        fun fromName(name: String?): ThemeMode = entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}

enum class AppLanguage(val localeTag: String?) {
    SYSTEM(null), ENGLISH("en"), ARABIC("ar");

    companion object {
        fun fromName(name: String?): AppLanguage = entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}

/** Everything the user can change in Settings, as one immutable object. */
data class AppSettings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val notificationsEnabled: Boolean = true
)
