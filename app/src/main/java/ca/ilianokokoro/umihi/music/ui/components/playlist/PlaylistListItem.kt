package ca.ilianokokoro.umihi.music.ui.components.playlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.ui.components.SquareImage

@Composable
fun PlaylistListItem(
    playlistInfo: PlaylistInfo,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { },
    trailingContent: @Composable (() -> Unit)? = null,
) {

    ListItem(
        modifier = modifier,
        onClick = onClick,
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .aspectRatio(1f)
            ) {
                if (!playlistInfo.isDownloadedPlaylist) {
                    SquareImage(
                        uri = playlistInfo.coverPath ?: playlistInfo.coverHref,
                        contentDescription = stringResource(R.string.playlist_cover),
                        modifier = Modifier.matchParentSize()

                    )
                } else {
                    OfflineThumbnail()
                }
            }
        },

        shapes = ListItemDefaults.shapes(
            shape = RoundedCornerShape(12.dp)
        ),
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        verticalAlignment = Alignment.CenterVertically,
        content = {
            Text(
                text = playlistInfo.title,
                modifier = Modifier.padding(top = 8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
            )
        }, supportingContent = {
            PlaylistSubtitle(playlist = playlistInfo)
        }

    )

}
