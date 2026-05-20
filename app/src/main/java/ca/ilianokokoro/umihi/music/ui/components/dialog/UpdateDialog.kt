package ca.ilianokokoro.umihi.music.ui.components.dialog

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.managers.VersionManager
import ca.ilianokokoro.umihi.music.models.Version
import ca.ilianokokoro.umihi.music.models.dto.GithubReleaseResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpdateDialog(scope: CoroutineScope) {
    val context = LocalContext.current
    val data = remember { mutableStateOf<GithubReleaseResponse?>(null) }

    var showDialog by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var isDownloading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        VersionManager.eventsFlow.collect { event ->
            when (event) {
                is VersionManager.ScreenEvent.UpdateAvailable -> {
                    data.value = event.githubReleaseResponse
                    showDialog = true
                }
            }
        }
    }

    if (showDialog) {
        val releaseInfo = data.value ?: return
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = stringResource(
                        if (releaseInfo.isBeta) {
                            R.string.beta_update_available
                        } else {
                            R.string.update_available
                        }
                    ),
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {

                    if (isDownloading) {
                        Text(
                            text = stringResource(R.string.downloading_update),
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (downloadProgress > 0f) {
                            LinearWavyProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            LinearWavyProgressIndicator(
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${(downloadProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text(
                            text = stringResource(
                                R.string.new_version_body,
                                releaseInfo.tagName.take(8)
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Text(
                            text = releaseInfo.cleanBody,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                }
            },
            confirmButton = {
                if (!isDownloading) {
                    TextButton(
                        onClick = {


                            scope.launch {

                                val granted = VersionManager.requestInstallPermissions(
                                    activity = context as ComponentActivity
                                )

                                if (!granted) {
                                    isDownloading = false
                                    return@launch
                                }

                                isDownloading = true
                                downloadProgress = 0f

                                val apkFile = VersionManager.downloadApk(
                                    context = context,
                                    release = releaseInfo,
                                    onProgress = { progress ->
                                        downloadProgress = progress
                                    }
                                )

                                VersionManager.installApk(
                                    context = context,
                                    apkFile = apkFile
                                )

                                isDownloading = false
                                showDialog = false
                            }


                        },
                        shapes = ButtonDefaults.shapes()
                    ) { Text(stringResource(R.string.download)) }
                }

            },
            dismissButton = {
                if (!isDownloading) {
                    TextButton(onClick = {
                        scope.launch {
                            VersionManager.ignoreUpdate(
                                version = Version(
                                    name = releaseInfo.versionName
                                )
                            )
                            showDialog = false
                        }
                    }, shapes = ButtonDefaults.shapes()) { Text(stringResource(R.string.ignore)) }
                }
            },
            properties = DialogProperties(
                dismissOnClickOutside = !isDownloading,
                dismissOnBackPress = !isDownloading
            )
        )
    }


}
