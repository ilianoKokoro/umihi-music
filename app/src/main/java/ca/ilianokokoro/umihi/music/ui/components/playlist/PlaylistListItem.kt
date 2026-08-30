package ca.ilianokokoro.umihi.music.ui.components.playlist

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.ui.components.SquareImage
import ca.ilianokokoro.umihi.music.ui.components.dialog.ConfirmDialog

@Composable
fun PlaylistListItem(
    playlist: Playlist,
    onUnhidePlaylist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showUnhideDialog by remember { mutableStateOf(false) }

    ListItem(
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
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
            IconButton(onClick = { showUnhideDialog = true }) {
                Icon(
                    Icons.Rounded.Visibility,
                    contentDescription = stringResource(R.string.unhide_playlist)
                )
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
                onUnhidePlaylist()
            },
            onDismiss = { showUnhideDialog = false }
        )
    }
}
