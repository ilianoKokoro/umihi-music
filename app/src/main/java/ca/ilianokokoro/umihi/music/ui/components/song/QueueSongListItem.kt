package ca.ilianokokoro.umihi.music.ui.components.song

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.components.SquareImage

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QueueSongListItem(
    song: Song,
    index: Int,
    onPress: () -> Unit,
    modifier: Modifier,
) {
    ListItem(
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .aspectRatio(1f)
            ) {
                SquareImage(song.thumbnailHref, modifier = Modifier.matchParentSize())
            }

        },
        trailingContent = {
            Icon(
                modifier = modifier,
                imageVector = Icons.Rounded.DragHandle,
                contentDescription = "Reorder"
            )
        },
        headlineContent = { Text(song.title, modifier = Modifier.basicMarquee()) },
        supportingContent = { Text(song.artist, modifier = Modifier.basicMarquee()) },
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onPress)
    )

}