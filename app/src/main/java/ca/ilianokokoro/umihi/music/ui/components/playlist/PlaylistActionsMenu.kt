package ca.ilianokokoro.umihi.music.ui.components.playlist

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
import ca.ilianokokoro.umihi.music.ui.components.materialu.dropdown.DropdownItem
import ca.ilianokokoro.umihi.music.ui.components.materialu.dropdown.MaterialUDropdown
import ca.ilianokokoro.umihi.music.ui.icons.bookmark_remove
import ca.ilianokokoro.umihi.music.ui.icons.delete
import ca.ilianokokoro.umihi.music.ui.icons.play_arrow
import ca.ilianokokoro.umihi.music.ui.icons.shuffle
import ca.ilianokokoro.umihi.music.ui.icons.visibility_off

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
        items = listOf(
            DropdownItem(
                icon = play_arrow,
                text = stringResource(R.string.play),
                onClick = onPlay,
            ),
            DropdownItem(
                icon = shuffle,
                text = stringResource(R.string.shuffle),
                onClick = onShuffle,
            ),
            DropdownItem(
                icon = visibility_off,
                text = stringResource(R.string.hide_playlist),
                onClick = { showHideDialog = true },
            ),
            DropdownItem(
                icon = delete,
                text = stringResource(R.string.delete_playlist),
                onClick = { showDeleteDialog = true },
                visible = playlistInfo.type == PlaylistType.CREATED_BY_USER,
            ),
            DropdownItem(
                icon = bookmark_remove,
                text = stringResource(R.string.remove_library),
                onClick = { showRemoveFromLibraryDialog = true },
                visible = playlistInfo.type == PlaylistType.SAVED,
            ),
        ),
    )
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
