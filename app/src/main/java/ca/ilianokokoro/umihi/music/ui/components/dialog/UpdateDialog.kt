package ca.ilianokokoro.umihi.music.ui.components.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
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
import ca.ilianokokoro.umihi.music.core.managers.VersionManager
import ca.ilianokokoro.umihi.music.models.Version
import ca.ilianokokoro.umihi.music.models.dto.GithubReleaseResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpdateDialog(scope: CoroutineScope) {
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
        val releaseInfo = data.value as GithubReleaseResponse
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = {
                Text(
                    text = stringResource(if (releaseInfo.isBeta) R.string.beta_update_available else R.string.update_available),
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = stringResource(
                            R.string.new_version_body,
                            releaseInfo.tagName
                        ),
                        style = MaterialTheme.typography.bodyLarge

                    )
                    Text(
                        text = releaseInfo.cleanBody,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
//                TextButton(
//                    onClick = {
//                        showDialog.value = false
//                        uriHandler.openUri(Constants.Url.GITHUB_RELEASE_LINK)
//                    },
//                    shapes = ButtonDefaults.shapes()
//                ) { Text(stringResource(R.string.open)) }

                TextButton(
                    onClick = {
                        showDialog.value = false
                        uriHandler.openUri(releaseInfo.downloadUrl)
                    },
                    shapes = ButtonDefaults.shapes()
                ) { Text(stringResource(R.string.download)) }

            },
            dismissButton = {
                TextButton(onClick = {
                    scope.launch {
                        VersionManager.ignoreUpdate(
                            version = Version(
                                name = releaseInfo.versionName
                            )
                        )
                        showDialog.value = false
                    }
                }, shapes = ButtonDefaults.shapes()) { Text(stringResource(R.string.ignore)) }
            },
            properties = DialogProperties(dismissOnClickOutside = true)
        )
    }


}
