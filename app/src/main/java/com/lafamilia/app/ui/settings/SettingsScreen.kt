package com.lafamilia.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lafamilia.app.ui.invite.InviteSection

/**
 * PLACEHOLDER from part 1 so the app compiles and navigates.
 * Part 5 (settings, theme, language) replaces this whole file.
 */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().systemBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Settings (placeholder, built in part 5)")
        InviteSection()
        Button(onClick = onBack) { Text("Back") }
    }
}
