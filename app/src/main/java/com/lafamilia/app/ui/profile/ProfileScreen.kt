package com.lafamilia.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lafamilia.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.profile_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Avatar(name = state.name)

            Text(
                text = state.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.profile_name)) },
                singleLine = true,
                isError = state.nameEmpty,
                supportingText = if (state.nameEmpty) {
                    { Text(stringResource(R.string.auth_err_name)) }
                } else null
            )
            OutlinedTextField(
                value = state.status,
                onValueChange = viewModel::onStatusChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.profile_status)) },
                maxLines = 3
            )

            Button(
                onClick = viewModel::save,
                enabled = !state.isSaving && !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.profile_save))
                }
            }

            if (state.saved) {
                Text(
                    text = stringResource(R.string.profile_saved),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (state.saveFailed) {
                Text(
                    text = stringResource(R.string.profile_save_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            OutlinedButton(
                onClick = {
                    viewModel.signOut()
                    onSignedOut()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.profile_sign_out), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/** A round badge with the first letter of the name. */
@Composable
private fun Avatar(name: String) {
    val initial = name.trim().firstOrNull()?.uppercase() ?: "?"
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
