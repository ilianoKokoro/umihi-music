package ca.ilianokokoro.umihi.music.error

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.ComposeHelper.findActivity
import ca.ilianokokoro.umihi.music.core.helpers.ComposeHelper.getShortErrorFromLog
import cat.ereza.customactivityoncrash.CustomActivityOnCrash

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorScreen() {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val activity = context.findActivity()
    val intent = activity?.intent

    val config = intent?.let { CustomActivityOnCrash.getConfigFromIntent(it) }
    val message = intent?.let {
        CustomActivityOnCrash.getAllErrorDetailsFromIntent(context, it)
    } ?: stringResource(R.string.unknown_error)


    var isDialogShown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.error_screen_title)) }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                tonalElevation = 2.dp,
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    contentDescription = stringResource(R.string.error),
                    modifier = Modifier
                        .size(72.dp)
                        .padding(16.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.user_error_request),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))




            FilledTonalButton(
                onClick = { isDialogShown = true },
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(stringResource(R.string.view_details))
            }

            Button(
                onClick = {
                    if (activity != null && config != null) {
                        CustomActivityOnCrash.restartApplication(activity, config)
                    }
                },
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(stringResource(R.string.restart_application))
            }

            OutlinedButton(
                onClick = {
                    runCatching {
                        uriHandler.openUri(Constants.Url.DISCORD_INVITE)
                    }
                },
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(stringResource(R.string.join_discord_server))
            }
        }
    }

    if (isDialogShown) {
        ErrorLogDialog(
            context = context,
            onDismissRequest = { isDialogShown = false },
            dialogTitle = stringResource(R.string.error_details),
            dialogText = message.getShortErrorFromLog(),
            fullErrorLog = message
        )
    }
}
