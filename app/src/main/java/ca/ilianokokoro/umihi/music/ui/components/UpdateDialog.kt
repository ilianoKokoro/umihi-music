@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package ca.ilianokokoro.umihi.music.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.managers.VersionManager
import ca.ilianokokoro.umihi.music.models.dto.GithubReleaseResponse

@Composable
fun UpdateDialog() {
    val uriHandler = LocalUriHandler.current

    val showDialog = remember { mutableStateOf(false) }
    val data = remember { mutableStateOf<GithubReleaseResponse?>(null) }

    LaunchedEffect(Unit) {
        VersionManager.eventsFlow.collect { event ->
            when (event) {
                is VersionManager.ScreenEvent.UpdateAvailable -> {
                    data.value = event.githubReleaseResponse
                    showDialog.value = true
                }
            }
        }
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(stringResource(R.string.update_available)) },
            text = {
                Text(
                    stringResource(
                        R.string.new_version_body,
                        data.value?.tagName.toString()
                    )
                )
            }, // TODO : add release changelog
            confirmButton = {},
            dismissButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        // TODO : save version as ignored
                        showDialog.value = false
                    }, shapes = ButtonDefaults.shapes()) { Text(stringResource(R.string.dismiss)) }


                    Row {
                        TextButton(
                            onClick = {
                                showDialog.value = false
                                uriHandler.openUri(Constants.Url.GITHUB_RELEASE_LINK)
                            },
                            shapes = ButtonDefaults.shapes()
                        ) { Text(stringResource(R.string.open)) }

//                        TextButton(
//                            onClick = {
//                                showDialog.value = false
//                                // TODO : download apk
//                            },
//                            shapes = ButtonDefaults.shapes()
//                        ) { Text(stringResource(R.string.download)) }
                    }
                }

            },
            properties = DialogProperties(dismissOnClickOutside = false)
        )
    }
}
