package com.lafamilia.app.ui.chatlist

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

/**
 * PLACEHOLDER from part 1 so the app compiles and navigates.
 * Part 2 (conversation list and search) replaces this whole file.
 */
@Composable
fun ChatListScreen(
    onOpenChat: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().systemBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Chat list (placeholder, built in part 2)")
        Button(onClick = { onOpenChat("demo-mom") }) { Text("Open demo chat") }
        Button(onClick = onOpenProfile) { Text("Profile") }
        Button(onClick = onOpenSettings) { Text("Settings") }
    }
}
