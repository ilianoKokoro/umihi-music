package ca.ilianokokoro.umihi.music.ui.components.song

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.components.SquareImage

@Composable
fun SongListItem(song: Song, onPress: () -> Unit, modifier: Modifier = Modifier) {


    ListItem(
        leadingContent = {
            Box(
                modifier = modifier
                    .size(60.dp)        // match the row height
                    .aspectRatio(1f)        // force square
            ) {
                SquareImage(song.thumbnailHref, modifier = modifier.matchParentSize())
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
                        modifier = modifier.size(16.dp),
                        imageVector = Icons.Rounded.DownloadForOffline,
                        contentDescription = Icons.Rounded.DownloadForOffline.toString()
                    )
                    Text(
                        stringResource(R.string.dot),
                        modifier = modifier.padding(horizontal = 3.dp)
                    )
                }

                Text(song.artist, modifier = modifier.basicMarquee())
            }
        },
        modifier = modifier.clickable(onClick = onPress)
    )


}