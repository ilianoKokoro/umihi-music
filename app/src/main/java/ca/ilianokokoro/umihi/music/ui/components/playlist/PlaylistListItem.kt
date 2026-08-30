package ca.ilianokokoro.umihi.music.ui.components.playlist

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.ui.components.SquareImage
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.ui.components.dialog.ConfirmDialog
import ca.ilianokokoro.umihi.music.ui.components.materialu.dropdown.MaterialUDropdown
import ca.ilianokokoro.umihi.music.ui.components.materialu.dropdown.MaterialUDropdownItem

@Composable
fun PlaylistListItem(
    modifier: Modifier = Modifier,
    playlist: Playlist,
    onPlaylistPressed: (PlaylistInfo) -> Unit,
    onUnhidePlaylist: (() -> Unit)? = null,
    onDeletePlaylist: (() -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    var showUnhideDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    ListItem(
        modifier = modifier.clickable {
            onPlaylistPressed(playlist.info)
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .aspectRatio(1f)
            ) {
                SquareImage(
                    uri = playlist.info.coverPath ?: playlist.info.coverHref,
                    modifier = Modifier.matchParentSize()
                )
            }
        },
        trailingContent = {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    Icons.Rounded.MoreVert,
                    contentDescription = stringResource(R.string.more)
                )
            }

            MaterialUDropdown(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (onUnhidePlaylist != null) {
                    MaterialUDropdownItem(
                        leadingIcon = Icons.Rounded.Visibility,
                        text = stringResource(R.string.unhide_playlist),
                        onClick = {
                            expanded = false
                            showUnhideDialog = true
                        }
                    )
                }

                if (onDeletePlaylist != null) {
                    MaterialUDropdownItem(
                        leadingIcon = Icons.Rounded.Delete,
                        text = stringResource(R.string.delete_playlist),
                        onClick = {
                            expanded = false
                            showDeleteDialog = true
                        }
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(),
        verticalAlignment = Alignment.CenterVertically,
        content = {
            Text(
                text = playlist.info.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.basicMarquee()
            )
        }
    )
    if (showUnhideDialog) {
        ConfirmDialog(
            title = stringResource(R.string.unhide_playlist),
            text = stringResource(R.string.unhide_playlist_confirm_text),
            onConfirm = {
                showUnhideDialog = false
                onUnhidePlaylist?.invoke()
            },
            onDismiss = { showUnhideDialog = false }
        )
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = stringResource(R.string.delete_playlist),
            text = stringResource(R.string.delete_playlist_text),
            onConfirm = {
                showDeleteDialog = false
                onDeletePlaylist?.invoke()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}