package com.lafamilia.app.ui.chatlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lafamilia.app.R
import com.lafamilia.app.domain.Conversation
import com.lafamilia.app.domain.User
import com.lafamilia.app.util.TimeFormat

/** Round badge with the first letter of a name. */
@Composable
internal fun Avatar(name: String, modifier: Modifier = Modifier) {
    val initial = name.trim().firstOrNull()?.uppercase() ?: "?"
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/** Unread counter. A muted chat still shows the count, but in a quiet color. */
@Composable
internal fun UnreadBadge(count: Int, muted: Boolean = false) {
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
            .clip(CircleShape)
            .background(if (muted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
        )
    }
}

/** Menu actions for one conversation (issue #42). */
internal class ConversationMenuActions(
    val onPin: () -> Unit,
    val onMute: () -> Unit,
    val onArchive: () -> Unit,
    val onRemove: () -> Unit,
    val onDismiss: () -> Unit
)

/** One line of the list. Tap opens the chat, long press opens the menu (pin, mute, archive, remove). */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ConversationRow(
    conversation: Conversation,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    menuOpen: Boolean = false,
    menu: ConversationMenuActions? = null
) {
    val placeholder = stringResource(R.string.chatlist_no_messages)
    val preview = conversation.lastMessageText.ifBlank { placeholder }
    val hasUnread = conversation.unreadCount > 0

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(conversation.title)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversation.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (hasUnread && !conversation.isMuted) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (conversation.isPinned) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = stringResource(R.string.chatlist_pinned),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (conversation.isMuted) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.chatlist_muted),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = TimeFormat.chatTime(conversation.lastMessageAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (hasUnread) UnreadBadge(conversation.unreadCount, muted = conversation.isMuted)
            }
        }
        if (menu != null) {
            DropdownMenu(expanded = menuOpen, onDismissRequest = menu.onDismiss) {
                DropdownMenuItem(
                    text = { Text(stringResource(if (conversation.isPinned) R.string.chatlist_unpin else R.string.chatlist_pin)) },
                    onClick = menu.onPin
                )
                DropdownMenuItem(
                    text = { Text(stringResource(if (conversation.isMuted) R.string.chatlist_unmute else R.string.chatlist_mute)) },
                    onClick = menu.onMute
                )
                DropdownMenuItem(
                    text = { Text(stringResource(if (conversation.isArchived) R.string.chatlist_unarchive else R.string.chatlist_archive)) },
                    onClick = menu.onArchive
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.chatlist_delete), color = MaterialTheme.colorScheme.error) },
                    onClick = menu.onRemove
                )
            }
        }
    }
}

/** Entry at the top of the main list that opens the archived chats. Hidden when nothing is archived. */
@Composable
internal fun ArchivedEntry(count: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.chatlist_archived),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun EmptyState(hasQuery: Boolean) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(if (hasQuery) R.string.chatlist_no_results else R.string.chatlist_empty_title),
                style = MaterialTheme.typography.titleMedium
            )
            if (!hasQuery) {
                Text(
                    text = stringResource(R.string.chatlist_empty_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun NewChatDialog(
    state: NewChatState,
    onQueryChange: (String) -> Unit,
    onPick: (User) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.chatlist_new_chat)) },
        text = {
            Column {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.chatlist_find_user_hint)) }
                )
                Spacer(Modifier.size(12.dp))
                when {
                    state.failed -> Text(
                        text = stringResource(R.string.chatlist_search_error),
                        color = MaterialTheme.colorScheme.error
                    )
                    state.isSearching -> CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    state.query.isNotBlank() && state.results.isEmpty() -> Text(stringResource(R.string.chatlist_no_users))
                    else -> LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                        items(state.results, key = { it.id }) { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPick(user) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Avatar(user.name)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(user.name, style = MaterialTheme.typography.titleMedium)
                                    if (user.status.isNotBlank()) {
                                        Text(
                                            text = user.status,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.chatlist_cancel)) }
        }
    )
}

@Composable
internal fun RemoveChatDialog(
    conversation: Conversation,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.chatlist_delete_title)) },
        text = { Text("${conversation.title}\n${stringResource(R.string.chatlist_delete_body)}") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.chatlist_delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.chatlist_cancel)) }
        }
    )
}
