package ca.ilianokokoro.umihi.music.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.models.Playlist

@Composable
fun PlaylistInfo(playlist: Playlist, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(150.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SquareImage(playlist.coverHref)
        Column {
            Text(
                modifier = modifier.fillMaxWidth(),
                text = playlist.title,
                style = MaterialTheme.typography.headlineSmall // TODO : Maybe change soon
            )
            Text(stringResource(R.string.songs, playlist.songs.count()))
        }

    }
}