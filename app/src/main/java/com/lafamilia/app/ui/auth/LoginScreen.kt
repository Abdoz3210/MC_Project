package com.lafamilia.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lafamilia.app.R

/** Stateful wrapper: connects the ViewModel to the stateless [LoginContent]. */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGoToRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoginSuccess()
    }

    LoginContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::login,
        onGoToRegister = onGoToRegister
    )
}

/** Stateless: draws whatever [state] says. Easy to preview and to reason about. */
@Composable
fun LoginContent(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onGoToRegister: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.auth_login_title),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = stringResource(R.string.auth_login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AuthTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = stringResource(R.string.auth_email),
                keyboardType = KeyboardType.Email,
                enabled = !state.isLoading
            )
            AuthTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = stringResource(R.string.auth_password),
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                isPassword = true,
                enabled = !state.isLoading
            )

            AuthErrorText(state.validationError, state.serverError)

            Button(
                onClick = onLoginClick,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(2.dp))
                } else {
                    Text(stringResource(R.string.auth_login_button))
                }
            }

            TextButton(onClick = onGoToRegister, enabled = !state.isLoading) {
                Text(stringResource(R.string.auth_go_register))
            }
        }
    }
}
