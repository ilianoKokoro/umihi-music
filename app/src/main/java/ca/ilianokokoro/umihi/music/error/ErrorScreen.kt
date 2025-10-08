@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@Composable
fun ErrorScreen() {
    val context = LocalContext.current
    val activity = context.findActivity()
    val intent = activity?.intent!!
    val uriHandler = LocalUriHandler.current

    val config = CustomActivityOnCrash.getConfigFromIntent(intent)!!
    val message = CustomActivityOnCrash.getAllErrorDetailsFromIntent(context, intent)

    val isDialogShown = remember { mutableStateOf(false) }

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
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
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
                onClick = {
                    uriHandler.openUri(Constants.Url.DISCORD_INVITE)
                },
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(stringResource(R.string.join_discord_server))
            }


            FilledTonalButton(
                onClick = { isDialogShown.value = true },
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(stringResource(R.string.view_details))
            }

            ElevatedButton(
                onClick = {
                    CustomActivityOnCrash.restartApplication(activity, config)
                },
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(stringResource(R.string.restart_application))
            }
        }
    }

    if (isDialogShown.value) {
        ErrorLogDialog(
            context = context,
            onDismissRequest = { isDialogShown.value = false },
            dialogTitle = stringResource(R.string.error_details),
            dialogText = message.getShortErrorFromLog(),
            fullErrorLog = message
        )
    }
}
