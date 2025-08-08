package ca.ilianokokoro.umihi.music.ui.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.models.Song

@Composable
fun SongRow(song: Song, onPress: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onPress, modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = modifier.padding(8.dp)) {
                SquareImage(song.lowQualityCoverHref)
            }
            Column(
                modifier = modifier
                    .padding(vertical = 4.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    modifier = modifier.basicMarquee(
                        repeatDelayMillis = Constants.Marquee.DELAY,
                        initialDelayMillis = Constants.Marquee.DELAY,
                    )
                )

                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    modifier = modifier.basicMarquee(
                        repeatDelayMillis = Constants.Marquee.DELAY,
                        initialDelayMillis = Constants.Marquee.DELAY,
                    )
                )
            }
        }
    }
}