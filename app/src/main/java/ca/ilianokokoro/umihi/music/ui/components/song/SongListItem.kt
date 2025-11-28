package ca.ilianokokoro.umihi.music.ui.components.song

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.components.SquareImage
import ca.ilianokokoro.umihi.music.ui.components.dropdown.ModernDropdownItem

@Composable
fun SongListItem(
    song: Song,
    onPress: () -> Unit,
    playNext: () -> Unit,
    addToQueue: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }


    Box {
        ListItem(
            leadingContent = {
                Box(
                    modifier = modifier
                        .size(60.dp)        // match the row height
                        .aspectRatio(1f)        // force square
                ) {
                    SquareImage(
                        song.thumbnailPath ?: song.thumbnailHref,
                        modifier = modifier.matchParentSize()
                    )
                }
            },
            headlineContent = { Text(song.title, modifier = modifier.basicMarquee()) },
            supportingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = modifier.fillMaxHeight()
                ) {
                    if (song.downloaded) {
                        Icon(
                            modifier = modifier
                                .padding(end = 3.dp)
                                .size(16.dp),
                            imageVector = Icons.Rounded.DownloadForOffline,
                            contentDescription = Icons.Rounded.DownloadForOffline.name,
                        )
                    }

                    Text(
                        "${song.artist} ${stringResource(R.string.dot)} ${song.duration}",
                        modifier = modifier.basicMarquee()
                    )
                }
            },
            trailingContent = {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        Icons.Rounded.MoreVert, contentDescription = stringResource(
                            R.string.more
                        )
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        ModernDropdownItem(
                            leadingIcon = Icons.Rounded.PlayCircleOutline,
                            text = stringResource(R.string.play_next),
                            onClick = {
                                playNext()
                                expanded = false
                            }
                        )
                        ModernDropdownItem(
                            leadingIcon = Icons.AutoMirrored.Rounded.PlaylistPlay,
                            text = stringResource(R.string.add_to_queue),
                            onClick = {
                                addToQueue()
                                expanded = false
                            }
                        )
                    }
                }
            },
            modifier = modifier
                .combinedClickable(onClick = onPress, onLongClick = {
                    expanded = true
                })
        )
    }


}