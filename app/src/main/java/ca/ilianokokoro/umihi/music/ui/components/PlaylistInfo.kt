package ca.ilianokokoro.umihi.music.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.models.Playlist

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlaylistInfo(
    playlist: Playlist,
    isDownloading: Boolean,
    onDownloadPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val songsCount = playlist.songs.count()
    var animatedCount by remember { mutableStateOf<Int?>(null) }
    val showDialog = remember { mutableStateOf(false) }

    LaunchedEffect(songsCount) {
        animatedCount = songsCount
    }

    Row(
        modifier = modifier
            .height(150.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SquareImage(playlist.info.coverPath ?: playlist.info.coverHref)
        Column(verticalArrangement = Arrangement.SpaceEvenly) {
            Column {
                Text(
                    modifier = modifier.fillMaxWidth(),
                    text = playlist.info.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                val alpha by animateFloatAsState(
                    targetValue = if (animatedCount == null || animatedCount == 0) 0f else 1f,
                    animationSpec = tween()
                )

                Text(
                    text = if (songsCount > 0) stringResource(
                        R.string.songs,
                        songsCount
                    ) else "",
                    modifier = Modifier.alpha(alpha)
                )


                if (isDownloading) {
                    CircularWavyProgressIndicator(modifier = Modifier.size(40.dp))
                } else {
                    FilledIconButton(
                        onClick = {
                            if (!playlist.downloaded) {
                                onDownloadPressed()
                            } else {
                                showDialog.value = true
                            }
                        },
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        enabled = alpha != 0F
                    ) {
                        if (playlist.downloaded) {
                            Icon(
                                imageVector = Icons.Rounded.DownloadDone,
                                contentDescription = null,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Download,
                                contentDescription = stringResource(R.string.download),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(stringResource(R.string.remove_local_playlist)) },
            text = { Text(stringResource(R.string.remove_local_confirm_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog.value = false
                        onDownloadPressed()
                    }
                ) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog.value = false }
                ) { Text(stringResource(R.string.cancel)) }
            },
            properties = DialogProperties(dismissOnClickOutside = false)
        )
    }


}
