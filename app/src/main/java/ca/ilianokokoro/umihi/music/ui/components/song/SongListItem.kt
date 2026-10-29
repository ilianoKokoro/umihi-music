package ca.ilianokokoro.umihi.music.ui.components.song

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.components.SquareImage
import ca.ilianokokoro.umihi.music.ui.components.materialu.dropdown.MaterialUDropdown
import ca.ilianokokoro.umihi.music.ui.components.materialu.dropdown.MaterialUDropdownItem
import ca.ilianokokoro.umihi.music.ui.icons.download as downloadIcon
import ca.ilianokokoro.umihi.music.ui.icons.more_vert
import ca.ilianokokoro.umihi.music.ui.icons.play_circle_outline
import ca.ilianokokoro.umihi.music.ui.icons.playlist_add
import ca.ilianokokoro.umihi.music.ui.icons.playlist_play
import ca.ilianokokoro.umihi.music.ui.icons.playlist_remove
import ca.ilianokokoro.umihi.music.ui.icons.radio

@Composable
fun SongListItem(
    song: Song,
    onPress: () -> Unit,
    playNext: () -> Unit,
    addToQueue: () -> Unit,
    modifier: Modifier = Modifier,
    download: (() -> Unit)? = null,
    addToPlaylist: (() -> Unit)? = null,
    removeFromPlaylist: (() -> Unit)? = null,
    isCurrentSong: Boolean = false,
    startMix: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ListItem(
        enabled = song.isAvailable,
        contentPadding = Constants.Ui.SongItems.PADDING,
        modifier = modifier
            .height(Constants.Ui.SongItems.ITEM_HEIGHT)
            .clip(
                Constants.Ui.SongItems.CORNER_RADIUS
            )
            .combinedClickable(
                enabled = song.isAvailable,
                onClick = onPress,
                onLongClick = { expanded = true }
            ),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(Constants.Ui.SongItems.IMAGE_SIZE)
                    .aspectRatio(1f)
            ) {
                SquareImage(
                    uri = song.thumbnailPath ?: song.thumbnailHref,
                    modifier = Modifier.matchParentSize()
                )
            }
        },
        trailingContent = {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    more_vert,
                    contentDescription = stringResource(R.string.more)
                )
            }
            MaterialUDropdown(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                MaterialUDropdownItem(
                    leadingIcon = play_circle_outline,
                    text = stringResource(R.string.play_next),
                    onClick = {
                        playNext()
                        expanded = false
                    }
                )
                MaterialUDropdownItem(
                    leadingIcon = playlist_play,
                    text = stringResource(R.string.add_to_queue),
                    onClick = {
                        addToQueue()
                        expanded = false
                    }
                )
                MaterialUDropdownItem(
                    leadingIcon = radio,
                    text = stringResource(R.string.mix_start),
                    onClick = {
                        startMix()
                        expanded = false
                    }
                )
                if (addToPlaylist != null) {
                    MaterialUDropdownItem(
                        leadingIcon = playlist_add,
                        text = stringResource(R.string.add_to_playlist),
                        onClick = {
                            addToPlaylist()
                            expanded = false
                        }
                    )
                }
                if (removeFromPlaylist != null) {
                    MaterialUDropdownItem(
                        leadingIcon = playlist_remove,
                        text = stringResource(R.string.remove_from_playlist),
                        onClick = {
                            removeFromPlaylist()
                            expanded = false
                        }
                    )
                }
                if (download != null && !song.downloaded) {
                    MaterialUDropdownItem(
                        leadingIcon = downloadIcon,
                        text = stringResource(R.string.download),
                        onClick = {
                            download()
                            expanded = false
                        }
                    )
                }
            }
        },
        supportingContent = {
            SongSubtitle(song)
        },
        colors = if (isCurrentSong) {
            ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        } else {
            ListItemDefaults.colors()
        },
        verticalAlignment = Alignment.CenterVertically,
        content = {
            Text(
                text = song.title,
                maxLines = 2,
                lineHeight = Constants.Ui.SongItems.TITLE_LINE_HEIGHT,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )
}
