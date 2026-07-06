package ca.ilianokokoro.umihi.music.ui.components.song

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Remove
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
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.components.SquareImage
import ca.ilianokokoro.umihi.music.ui.components.materialu.dropdown.MaterialUDropdown
import ca.ilianokokoro.umihi.music.ui.components.materialu.dropdown.MaterialUDropdownItem
import sh.calvin.reorderable.ReorderableCollectionItemScope


@Composable
fun QueueSongListItem(
    song: Song,
    isCurrentSong: Boolean,
    onPress: () -> Unit,
    onDelete: () -> Unit,
    scope: ReorderableCollectionItemScope,
    onDragStarted: () -> Unit,
    onDragStopped: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val innerHeight = 60.dp

    ListItem(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onPress),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(innerHeight)
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
                        Icons.Rounded.MoreVert, contentDescription = stringResource(
                            R.string.more
                        )
                    )

                    MaterialUDropdown(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {

                        MaterialUDropdownItem(
                            leadingIcon = Icons.Rounded.Remove,
                            text = "Remove from queue",
                            onClick = {
                                onDelete()
                                expanded = false
                            }
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Rounded.DragHandle,
                    contentDescription = stringResource(R.string.reorder),
                    modifier =
                        with(scope) {
                            Modifier
                                .height(innerHeight)
                                .draggableHandle(
                                    onDragStarted = { onDragStarted() },
                                    onDragStopped =
                                        onDragStopped,
                                )
                        },
                )
            }
        },
        supportingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                if (song.isExplicit) {
                    ExplicitBadge()
                }

                Text(
                    "${song.artist} ${stringResource(R.string.dot)} ${song.duration}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee()
                )
            }
        },
        colors = if (isCurrentSong) {
            ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        } else {
            ListItemDefaults.colors()
        },
        verticalAlignment = Alignment.CenterVertically,
        content = { Text(song.title, modifier = Modifier.basicMarquee()) },
    )

}