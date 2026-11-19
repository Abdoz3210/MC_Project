package com.lafamilia.app.ui.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lafamilia.app.data.settings.AppSettings
import com.lafamilia.app.data.settings.SettingsRepository
import com.lafamilia.app.data.settings.ThemeMode
import com.lafamilia.app.navigation.AppNavHost
import com.lafamilia.app.ui.theme.LaFamiliaTheme

/**
 * The root of the UI. It reads the saved settings from DataStore and applies
 * the chosen language and theme to the whole app, so a change in Settings shows instantly.
 */
@Composable
fun AppRoot() {
    val context = LocalContext.current
    val repository = remember { SettingsRepository.from(context) }
    val settings by repository.settings.collectAsStateWithLifecycle(initialValue = AppSettings())

    val darkTheme = when (settings.theme) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    LocaleWrapper(language = settings.language) {
        LaFamiliaTheme(darkTheme = darkTheme) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                AppNavHost()
            }
        }
    }
}
