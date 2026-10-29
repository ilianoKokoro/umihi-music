package ca.ilianokokoro.umihi.music.ui.components.playlist

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.PlaylistType
import ca.ilianokokoro.umihi.music.ui.components.SquareImage
import ca.ilianokokoro.umihi.music.ui.components.materialu.dropdown.MaterialUDropdown
import ca.ilianokokoro.umihi.music.ui.components.materialu.dropdown.MaterialUDropdownItem
import ca.ilianokokoro.umihi.music.ui.icons.bookmark_remove
import ca.ilianokokoro.umihi.music.ui.icons.cancel
import ca.ilianokokoro.umihi.music.ui.icons.delete
import ca.ilianokokoro.umihi.music.ui.icons.download
import ca.ilianokokoro.umihi.music.ui.icons.download_done
import ca.ilianokokoro.umihi.music.ui.icons.file_download_off
import ca.ilianokokoro.umihi.music.ui.icons.more_vert
import ca.ilianokokoro.umihi.music.ui.icons.sensors
import ca.ilianokokoro.umihi.music.ui.icons.visibility
import ca.ilianokokoro.umihi.music.ui.icons.visibility_off

@Composable
fun PlaylistInfo(
    playlist: Playlist,
    isDownloading: Boolean,
    onDownloadPressed: () -> Unit,
    onDeleteDownloadPressed: () -> Unit,
    onDeletePressed: () -> Unit,
    onRemoveFromLibraryPressed: () -> Unit,
    onCancelDownload: () -> Unit,
    onUnhidePlaylist: () -> Unit,
    onHidePlaylist: () -> Unit,
    onStartMix: () -> Unit = {},
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    optionsExtended: Boolean = false,
    onOptionsExtendedChange: (Boolean) -> Unit = {},
) {
    val totalCount = playlist.info.songCount
    val songsCount = if (isLoading) totalCount ?: 0 else playlist.songs.count()
    var animatedCount by remember { mutableStateOf<Int?>(null) }
    val showDeleteDownloadDialog = remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf(false) }
    val showCancelDialog = remember { mutableStateOf(false) }
    val showHideDialog = remember { mutableStateOf(false) }
    val showUnhideDialog = remember { mutableStateOf(false) }
    val showRemoveFromLibraryDialog = remember { mutableStateOf(false) }

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
        if (!playlist.info.isDownloadedPlaylist) {
            SquareImage(
                uri = playlist.info.coverPath ?: playlist.info.coverHref
            )
        } else {
            Icon(
                imageVector = download,
                contentDescription = null,
                modifier = Modifier.size(150.dp)
            )
        }
        Column(verticalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxHeight()) {
            val alpha by animateFloatAsState(
                targetValue = if (animatedCount == null || animatedCount == 0) {
                    0f
                } else {
                    1f
                },
                animationSpec = tween()
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = playlist.info.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )


                Text(
                    text = if (songsCount > 0) {
                        stringResource(R.string.songs, songsCount)
                    } else {
                        ""
                    },
                    modifier = Modifier.alpha(alpha)
                )

            }

            if (!playlist.info.isDownloadedPlaylist) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Row {
                        FilledIconButton(
                            onClick = {
                                if (playlist.downloaded) {
                                    showDeleteDownloadDialog.value = true
                                } else if (isDownloading) {
                                    showCancelDialog.value = true
                                } else {
                                    onDownloadPressed()
                                }
                            },
                            shapes = IconButtonDefaults.shapes(),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            enabled = !isLoading && alpha != 0F
                        ) {
                            if (playlist.downloaded) {
                                Icon(
                                    imageVector = download_done,
                                    contentDescription = null,
                                )
                            } else if (isDownloading) {
                                CircularWavyProgressIndicator(modifier = Modifier.size(25.dp))
                            } else {
                                Icon(
                                    imageVector = download,
                                    contentDescription = stringResource(R.string.download),
                                )
                            }
                        }

                        FilledIconButton(
                            onClick = onStartMix,
                            shapes = IconButtonDefaults.shapes(),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            enabled = !isLoading && alpha != 0F && playlist.songs.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = sensors,
                                contentDescription = stringResource(R.string.mix_start),
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            onOptionsExtendedChange(true)
                        },
                        shapes = IconButtonDefaults.shapes(),
                    ) {
                        Icon(
                            imageVector = more_vert,
                            contentDescription = stringResource(R.string.actions)
                        )

                        MaterialUDropdown(
                            expanded = optionsExtended,
                            onDismissRequest = { onOptionsExtendedChange(false) },
                        ) {
                            if (playlist.songs.isNotEmpty()) {
                                MaterialUDropdownItem(
                                    leadingIcon = sensors,
                                    text = stringResource(R.string.mix_start),
                                    onClick = {
                                        onStartMix()
                                        onOptionsExtendedChange(false)
                                    }
                                )
                            }

                            if (isDownloading) {
                                MaterialUDropdownItem(
                                    leadingIcon = cancel,
                                    text = stringResource(R.string.cancel_download),
                                    onClick = {
                                        showCancelDialog.value = true
                                        onOptionsExtendedChange(false)
                                    }
                                )
                            } else if (!playlist.downloaded) {
                                MaterialUDropdownItem(
                                    leadingIcon = download,
                                    text = stringResource(R.string.download),
                                    onClick = {
                                        onDownloadPressed()
                                        onOptionsExtendedChange(false)
                                    }
                                )
                            }

                            MaterialUDropdownItem(
                                leadingIcon = file_download_off,
                                text = stringResource(R.string.remove_download),
                                onClick = {
                                    showDeleteDownloadDialog.value = true
                                    onOptionsExtendedChange(false)
                                }
                            )

                            if (playlist.info.hidden) {
                                MaterialUDropdownItem(
                                    leadingIcon = visibility,
                                    text = stringResource(R.string.unhide_playlist),
                                    onClick = {
                                        showUnhideDialog.value = true
                                        onOptionsExtendedChange(false)
                                    }
                                )
                            } else if (!playlist.info.hidden) {
                                MaterialUDropdownItem(
                                    leadingIcon = visibility_off,
                                    text = stringResource(R.string.hide_playlist),
                                    onClick = {
                                        showHideDialog.value = true
                                        onOptionsExtendedChange(false)
                                    }
                                )
                            }

                            when (playlist.info.type) {
                                PlaylistType.CREATED_BY_USER -> {
                                    MaterialUDropdownItem(
                                        leadingIcon = delete,
                                        text = stringResource(R.string.delete_playlist),
                                        onClick = {
                                            showDeleteDialog.value = true
                                            onOptionsExtendedChange(false)
                                        }
                                    )
                                }

                                PlaylistType.SAVED -> {
                                    MaterialUDropdownItem(
                                        leadingIcon = bookmark_remove,
                                        text = stringResource(R.string.remove_library),
                                        onClick = {
                                            showRemoveFromLibraryDialog.value = true
                                            onOptionsExtendedChange(false)
                                        }
                                    )
                                }

                                else -> {}

                            }

                        }
                    }
                }
            }
        }

    }

    if (showDeleteDownloadDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDownloadDialog.value = false },
            title = { Text(stringResource(R.string.remove_local_playlist)) },
            text = { Text(stringResource(R.string.remove_local_confirm_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDownloadDialog.value = false
                        onDeleteDownloadPressed()
                    }
                ) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDownloadDialog.value = false }
                ) { Text(stringResource(R.string.cancel)) }
            },
            properties = DialogProperties(dismissOnClickOutside = false)
        )
    }

    if (showCancelDialog.value) {
        AlertDialog(
            onDismissRequest = { showCancelDialog.value = false },
            title = { Text(stringResource(R.string.cancel_playlist_download)) },
            text = { Text(stringResource(R.string.cancel_playlist_download_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog.value = false
                        onCancelDownload()
                    }
                ) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCancelDialog.value = false }
                ) { Text(stringResource(R.string.cancel)) }
            },
            properties = DialogProperties(dismissOnClickOutside = false)
        )
    }

    if (showUnhideDialog.value) {
        AlertDialog(
            onDismissRequest = { showUnhideDialog.value = false },
            title = { Text(stringResource(R.string.unhide_playlist)) },
            text = { Text(stringResource(R.string.unhide_playlist_confirm_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnhideDialog.value = false
                        onUnhidePlaylist()
                    }
                ) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUnhideDialog.value = false }
                ) { Text(stringResource(R.string.cancel)) }
            },
            properties = DialogProperties(dismissOnClickOutside = false)
        )
    }

    if (showHideDialog.value) {
        AlertDialog(
            onDismissRequest = { showHideDialog.value = false },
            title = { Text(stringResource(R.string.hide_playlist)) },
            text = { Text(stringResource(R.string.hide_playlist_confirm_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showHideDialog.value = false
                        onHidePlaylist()
                    }
                ) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showHideDialog.value = false }
                ) { Text(stringResource(R.string.cancel)) }
            },
            properties = DialogProperties(dismissOnClickOutside = false)
        )
    }

    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text(stringResource(R.string.delete_playlist)) },
            text = { Text(stringResource(R.string.delete_playlist_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog.value = false
                        onDeletePressed()
                    }
                ) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog.value = false }
                ) { Text(stringResource(R.string.cancel)) }
            },
            properties = DialogProperties(dismissOnClickOutside = false)
        )
    }

    if (showRemoveFromLibraryDialog.value) {
        AlertDialog(
            onDismissRequest = { showRemoveFromLibraryDialog.value = false },
            title = { Text(stringResource(R.string.remove_library)) },
            text = { Text(stringResource(R.string.remove_library_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRemoveFromLibraryDialog.value = false
                        onRemoveFromLibraryPressed()
                    }
                ) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRemoveFromLibraryDialog.value = false }
                ) { Text(stringResource(R.string.cancel)) }
            },
            properties = DialogProperties(dismissOnClickOutside = false)
        )
    }


}
