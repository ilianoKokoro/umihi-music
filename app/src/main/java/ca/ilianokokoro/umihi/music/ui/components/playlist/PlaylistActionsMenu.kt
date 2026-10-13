package ca.ilianokokoro.umihi.music.ui.components.playlist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkRemove
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.PlaylistType
import ca.ilianokokoro.umihi.music.ui.components.materialu.dropdown.MaterialUDropdown
import ca.ilianokokoro.umihi.music.ui.components.materialu.dropdown.MaterialUDropdownItem

@Composable
fun PlaylistActionsMenu(
    playlistInfo: PlaylistInfo,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onHide: () -> Unit,
    onDelete: () -> Unit,
    onRemoveFromLibrary: () -> Unit,
) {
    var showHideDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRemoveFromLibraryDialog by remember { mutableStateOf(false) }

    MaterialUDropdown(
        expanded = expanded,
        onDismissRequest = { onExpandedChange(false) },
    ) {
        MaterialUDropdownItem(
            leadingIcon = Icons.Rounded.PlayArrow,
            text = stringResource(R.string.play),
            onClick = {
                onPlay()
                onExpandedChange(false)
            }
        )

        MaterialUDropdownItem(
            leadingIcon = Icons.Rounded.Shuffle,
            text = stringResource(R.string.shuffle),
            onClick = {
                onShuffle()
                onExpandedChange(false)
            }
        )

        MaterialUDropdownItem(
            leadingIcon = Icons.Rounded.VisibilityOff,
            text = stringResource(R.string.hide_playlist),
            onClick = {
                showHideDialog = true
                onExpandedChange(false)
            }
        )

        when (playlistInfo.type) {
            PlaylistType.CREATED_BY_USER -> {
                MaterialUDropdownItem(
                    leadingIcon = Icons.Rounded.Delete,
                    text = stringResource(R.string.delete_playlist),
                    onClick = {
                        showDeleteDialog = true
                        onExpandedChange(false)
                    }
                )
            }

            PlaylistType.SAVED -> {
                MaterialUDropdownItem(
                    leadingIcon = Icons.Rounded.BookmarkRemove,
                    text = stringResource(R.string.remove_library),
                    onClick = {
                        showRemoveFromLibraryDialog = true
                        onExpandedChange(false)
                    }
                )
            }

            else -> {}
        }
    }

    if (showHideDialog) {
        ConfirmAlertDialog(
            title = stringResource(R.string.hide_playlist),
            text = stringResource(R.string.hide_playlist_confirm_text),
            onConfirm = {
                showHideDialog = false
                onHide()
            },
            onDismiss = { showHideDialog = false }
        )
    }

    if (showDeleteDialog) {
        ConfirmAlertDialog(
            title = stringResource(R.string.delete_playlist),
            text = stringResource(R.string.delete_playlist_text),
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showRemoveFromLibraryDialog) {
        ConfirmAlertDialog(
            title = stringResource(R.string.remove_library),
            text = stringResource(R.string.remove_library_text),
            onConfirm = {
                showRemoveFromLibraryDialog = false
                onRemoveFromLibrary()
            },
            onDismiss = { showRemoveFromLibraryDialog = false }
        )
    }
}

@Composable
private fun ConfirmAlertDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
        properties = DialogProperties(dismissOnClickOutside = false)
    )
}
