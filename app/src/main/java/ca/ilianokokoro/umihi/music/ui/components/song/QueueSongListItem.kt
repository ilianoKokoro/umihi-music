package ca.ilianokokoro.umihi.music.ui.components.song

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.components.SquareImage

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QueueSongListItem(song: Song, index: Int, onPress: () -> Unit, modifier: Modifier = Modifier) {
    ListItem(
        leadingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (index + 1).toString(),
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        textAlign = TextAlign.Center
                    )
                }
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .aspectRatio(1f)
                ) {
                    SquareImage(song.thumbnailHref, modifier = Modifier.matchParentSize())
                }
            }
        },
        headlineContent = { Text(song.title, modifier = Modifier.basicMarquee()) },
        supportingContent = { Text(song.artist, modifier = Modifier.basicMarquee()) },
        modifier = Modifier.clickable(onClick = onPress)
    )

}