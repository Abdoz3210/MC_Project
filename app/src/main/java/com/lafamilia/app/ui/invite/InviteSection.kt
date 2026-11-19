package com.lafamilia.app.ui.invite

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lafamilia.app.AppGraph
import com.lafamilia.app.R
import com.lafamilia.app.util.IntentHelper
import com.lafamilia.app.util.InviteText

/** "Invite friends" and "Send feedback" buttons. Both launch other apps through Intents. */
@Composable
fun InviteSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val appName = stringResource(R.string.app_name)
    val chooserTitle = stringResource(R.string.invite_chooser_title)
    val noApp = stringResource(R.string.invite_no_app)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.invite_title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.invite_body), style = MaterialTheme.typography.bodyMedium)

        Button(
            onClick = {
                val text = InviteText.invitation(appName, AppGraph.authRepository.displayName)
                if (!IntentHelper.shareText(context, chooserTitle, text)) {
                    Toast.makeText(context, noApp, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.invite_button)) }

        OutlinedButton(
            onClick = {
                val sent = IntentHelper.sendEmail(
                    context = context,
                    address = InviteText.FEEDBACK_EMAIL,
                    subject = InviteText.feedbackSubject(appName),
                    body = InviteText.feedbackBody("1.0", Build.VERSION.SDK_INT)
                )
                if (!sent) Toast.makeText(context, noApp, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.invite_feedback_button)) }
    }
}
