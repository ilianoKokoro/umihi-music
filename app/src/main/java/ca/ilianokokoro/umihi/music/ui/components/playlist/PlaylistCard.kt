package ca.ilianokokoro.umihi.music.ui.components.playlist

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.ui.components.SquareImage

@Composable
fun PlaylistCard(
    playlistInfo: PlaylistInfo,
    onClicked: () -> Unit,
    modifier: Modifier = Modifier,
    songCount: Int? = playlistInfo.songCount,
    onPlay: () -> Unit = {},
    onShuffle: () -> Unit = {},
    onHide: () -> Unit = {},
    onDelete: () -> Unit = {},
    onRemoveFromLibrary: () -> Unit = {},
) {
    val showMenu = !playlistInfo.isDownloadedPlaylist
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.medium)
                .combinedClickable(
                    onClick = onClicked,
                    onLongClick = { if (showMenu) expanded = true }
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxSize()
            ) {
                if (!playlistInfo.isDownloadedPlaylist) {
                    SquareImage(
                        uri = playlistInfo.coverPath ?: playlistInfo.coverHref,
                        contentDescription = stringResource(R.string.playlist_cover)
                    )
                } else {
                    OfflineThumbnail()
                }

                Text(
                    text = playlistInfo.title,
                    modifier = Modifier.padding(top = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = songCount?.let { count ->
                        stringResource(R.string.songs, count)
                    }.orEmpty(),
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (showMenu) {
            IconButton(
                onClick = { expanded = true },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = stringResource(R.string.actions)
                )

                PlaylistActionsMenu(
                    playlistInfo = playlistInfo,
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    onPlay = onPlay,
                    onShuffle = onShuffle,
                    onHide = onHide,
                    onDelete = onDelete,
                    onRemoveFromLibrary = onRemoveFromLibrary,
                )
            }
        }
    }
}
