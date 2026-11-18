package com.lafamilia.app.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.lafamilia.app.R
import com.lafamilia.app.domain.AuthError
import com.lafamilia.app.domain.PasswordStrength

/** One text field used by both the login and the register screen. */
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isPassword: Boolean = false,
    enabled: Boolean = true
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val hidden = isPassword && !passwordVisible

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        visualTransformation = if (hidden) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (isPassword) {
            {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(stringResource(if (passwordVisible) R.string.auth_hide else R.string.auth_show))
                }
            }
        } else null
    )
}

/** Maps a validation problem to its translated message. */
@Composable
fun AuthError.message(): String = stringResource(
    when (this) {
        AuthError.EMPTY_FIELDS -> R.string.auth_err_empty
        AuthError.NAME_REQUIRED -> R.string.auth_err_name
        AuthError.INVALID_EMAIL -> R.string.auth_err_email
        AuthError.PASSWORD_TOO_SHORT -> R.string.auth_err_password_short
        AuthError.PASSWORDS_DO_NOT_MATCH -> R.string.auth_err_mismatch
    }
)

/** Shows our validation message, or Firebase's message, or nothing. */
@Composable
fun AuthErrorText(validationError: AuthError?, serverError: String?, modifier: Modifier = Modifier) {
    val generic = stringResource(R.string.auth_err_generic)
    val message = validationError?.message() ?: serverError?.let { it.ifBlank { generic } }
    if (message != null) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier.fillMaxWidth()
        )
    }
}

/** A small bar + label that tells the user how strong the typed password is. */
@Composable
fun PasswordStrengthBar(strength: PasswordStrength, modifier: Modifier = Modifier) {
    val color = when (strength) {
        PasswordStrength.WEAK -> MaterialTheme.colorScheme.error
        PasswordStrength.MEDIUM -> MaterialTheme.colorScheme.tertiary
        PasswordStrength.STRONG -> MaterialTheme.colorScheme.primary
    }
    val fraction = when (strength) {
        PasswordStrength.WEAK -> 0.33f
        PasswordStrength.MEDIUM -> 0.66f
        PasswordStrength.STRONG -> 1f
    }
    val label = stringResource(
        when (strength) {
            PasswordStrength.WEAK -> R.string.auth_strength_weak
            PasswordStrength.MEDIUM -> R.string.auth_strength_medium
            PasswordStrength.STRONG -> R.string.auth_strength_strong
        }
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress = { fraction },
                color = color,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = label,
                color = color,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
        Text(
            text = stringResource(R.string.auth_password_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
