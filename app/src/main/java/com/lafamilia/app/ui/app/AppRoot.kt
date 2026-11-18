package com.lafamilia.app.ui.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lafamilia.app.navigation.AppNavHost
import com.lafamilia.app.ui.theme.LaFamiliaTheme

/**
 * The root of the UI: theme + background + navigation.
 * Part 5 replaces this file to add the saved theme and language from Settings.
 */
@Composable
fun AppRoot() {
    LaFamiliaTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            AppNavHost()
        }
    }
}
