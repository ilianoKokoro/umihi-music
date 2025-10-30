package ca.ilianokokoro.umihi.music.ui.components.song

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.components.SquareImage
import sh.calvin.reorderable.ReorderableCollectionItemScope

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QueueSongListItem(
    song: Song,
    isCurrentSong: Boolean,
    onPress: () -> Unit,
    scope: ReorderableCollectionItemScope,
    onDragStarted: () -> Unit,
    onDragStopped: () -> Unit
) {
    val innerHeight = 60.dp

    ListItem(
        colors =
            if (isCurrentSong)
                ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            else
                ListItemDefaults.colors(),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(innerHeight)
                    .aspectRatio(1f)
            ) {
                SquareImage(song.thumbnailHref, modifier = Modifier.matchParentSize())
            }

        },
        trailingContent = {
            Column(
                verticalArrangement = Arrangement.Center,
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
            ) {
                Icon(
                    imageVector = Icons.Rounded.DragHandle,
                    contentDescription = "Reorder"
                )
            }
        },
        headlineContent = { Text(song.title, modifier = Modifier.basicMarquee()) },
        supportingContent = { Text(song.artist, modifier = Modifier.basicMarquee()) },
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onPress)
    )

}