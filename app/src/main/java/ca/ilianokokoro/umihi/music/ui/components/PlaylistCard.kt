package ca.ilianokokoro.umihi.music.ui.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.models.Playlist

@Composable
fun PlaylistCard(onClicked: () -> Unit, playlist: Playlist) {
    Card(onClick = onClicked, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize()
        ) {
            SquareImage(
                url = playlist.coverHref,
            )
            Text(
                playlist.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .basicMarquee(
                        repeatDelayMillis = Constants.Marquee.DELAY,
                        initialDelayMillis = Constants.Marquee.DELAY,
                    ),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center

            )
        }
    }
}