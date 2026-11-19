package com.lafamilia.app.ui.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lafamilia.app.R
import com.lafamilia.app.domain.Message
import com.lafamilia.app.domain.MessageStatus
import com.lafamilia.app.util.TimeFormat

@Composable
private fun statusText(status: MessageStatus): String = stringResource(
    when (status) {
        MessageStatus.QUEUED -> R.string.chat_status_queued
        MessageStatus.SENDING -> R.string.chat_status_sending
        MessageStatus.SENT -> R.string.chat_status_sent
        MessageStatus.DELIVERED -> R.string.chat_status_delivered
        MessageStatus.READ -> R.string.chat_status_read
        MessageStatus.FAILED -> R.string.chat_status_failed
    }
)

/** One chat bubble. Mine sit on the end side (right in English, left in Arabic). */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MessageBubble(
    message: Message,
    mine: Boolean,
    onLongClick: () -> Unit,
    onRetry: () -> Unit
) {
    val container = if (mine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val content = if (mine) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val failed = message.status == MessageStatus.FAILED

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = container,
            contentColor = content,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .widthIn(max = 300.dp)
                .combinedClickable(onClick = { if (failed) onRetry() }, onLongClick = onLongClick)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(text = message.body, style = MaterialTheme.typography.bodyLarge)
                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = TimeFormat.chatTime(message.createdAt),
                        style = MaterialTheme.typography.labelSmall
                    )
                    if (mine) {
                        Text(
                            text = if (failed) "${statusText(message.status)} - ${stringResource(R.string.chat_tap_retry)}"
                            else statusText(message.status),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (failed) MaterialTheme.colorScheme.error else content
                        )
                    }
                }
            }
        }
    }
}

/** Menu shown after a long press on one of your own messages. */
@Composable
internal fun MessageActionsDialog(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.chat_message_actions)) },
        text = {
            Column {
                TextButton(onClick = onEdit) { Text(stringResource(R.string.chat_edit)) }
                TextButton(onClick = onDelete) {
                    Text(stringResource(R.string.chat_delete), color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.chat_cancel)) } }
    )
}

@Composable
internal fun DeleteMessageDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.chat_delete_title)) },
        text = { Text(stringResource(R.string.chat_delete_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.chat_delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.chat_cancel)) } }
    )
}
