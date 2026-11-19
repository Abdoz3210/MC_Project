package com.lafamilia.app.ui.chatlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lafamilia.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onOpenChat: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: ChatListViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // The ViewModel asks to open a chat (after "new chat"); we navigate once, then clear the request.
    LaunchedEffect(state.openChatId) {
        state.openChatId?.let { conversationId ->
            onOpenChat(conversationId)
            viewModel.onChatOpened()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onOpenProfile) {
                        Icon(Icons.Default.Person, contentDescription = stringResource(R.string.chatlist_profile))
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.chatlist_settings))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::openNewChat) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.chatlist_new_chat))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.chatlist_search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            val visible = state.visibleConversations
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                visible.isEmpty() -> EmptyState(hasQuery = state.query.isNotBlank())
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(visible, key = { it.id }) { conversation ->
                        ConversationRow(
                            conversation = conversation,
                            onClick = { onOpenChat(conversation.id) },
                            onLongClick = { viewModel.askDelete(conversation) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    state.newChat?.let { newChat ->
        NewChatDialog(
            state = newChat,
            onQueryChange = viewModel::onUserQueryChange,
            onPick = viewModel::startChatWith,
            onDismiss = viewModel::closeNewChat
        )
    }

    state.pendingDelete?.let { conversation ->
        RemoveChatDialog(
            conversation = conversation,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete
        )
    }
}
