package ca.ilianokokoro.umihi.music.ui.components

import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ca.ilianokokoro.umihi.music.models.Playlist

@Composable
fun PlaylistCard(onClicked: () -> Unit, playlist: Playlist, modifier: Modifier = Modifier) {
    Card(onClick = onClicked) {
        Text(playlist.title)
    }
}