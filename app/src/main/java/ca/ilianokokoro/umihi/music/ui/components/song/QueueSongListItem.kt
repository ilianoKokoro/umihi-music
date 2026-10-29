package ca.ilianokokoro.umihi.music.ui.components.song

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import ca.ilianokokoro.umihi.music.ui.icons.drag_handle
import ca.ilianokokoro.umihi.music.ui.icons.more_vert
import ca.ilianokokoro.umihi.music.ui.icons.radio
import ca.ilianokokoro.umihi.music.ui.icons.remove
import sh.calvin.reorderable.ReorderableCollectionItemScope


@Composable
fun QueueSongListItem(
    song: Song,
    isCurrentSong: Boolean,
    onPress: () -> Unit,
    onDelete: () -> Unit,
    startMix: () -> Unit,
    scope: ReorderableCollectionItemScope? = null,
    onDragStarted: () -> Unit = {},
    onDragStopped: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    ListItem(
        enabled = song.isAvailable,
        onClick = onPress,
        contentPadding = Constants.Ui.SongItems.PADDING,
        modifier = Modifier
            .height(Constants.Ui.SongItems.ITEM_HEIGHT)
            .clip(Constants.Ui.SongItems.CORNER_RADIUS),
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {


                IconButton(onClick = { expanded = true }) {
                    Icon(
                        more_vert, contentDescription = stringResource(
                            R.string.more
                        )
                    )

                    MaterialUDropdown(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {

                        MaterialUDropdownItem(
                            leadingIcon = radio,
                            text = stringResource(R.string.mix_start),
                            onClick = {
                                startMix()
                                expanded = false
                            }
                        )

                        MaterialUDropdownItem(
                            leadingIcon = remove,
                            text = stringResource(R.string.remove_from_queue),
                            onClick = {
                                onDelete()
                                expanded = false
                            }
                        )
                    }
                }

                if (scope != null) {
                    Icon(
                        imageVector = drag_handle,
                        contentDescription = stringResource(R.string.reorder),
                        modifier =
                            with(scope) {
                                Modifier
                                    .height(Constants.Ui.SongItems.ITEM_HEIGHT)
                                    .draggableHandle(
                                        onDragStarted = { onDragStarted() },
                                        onDragStopped =
                                            onDragStopped,
                                    )
                            },
                    )
                }
            }
        },
        supportingContent = {
            SongSubtitle(song)
        },
        colors = if (isCurrentSong) {
            ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        } else {
            ListItemDefaults.colors()
        },
        verticalAlignment = Alignment.CenterVertically,
        content = {
            Text(
                song.title,
                maxLines = 2,
                lineHeight = Constants.Ui.SongItems.TITLE_LINE_HEIGHT,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )

}
