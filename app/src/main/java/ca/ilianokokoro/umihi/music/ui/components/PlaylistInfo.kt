package ca.ilianokokoro.umihi.music.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.models.Playlist

@Composable
fun PlaylistInfo(playlist: Playlist, onDownloadPressed: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(150.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SquareImage(playlist.coverHref)
        Column(verticalArrangement = Arrangement.SpaceEvenly) {
            Column {
                Text(
                    modifier = modifier.fillMaxWidth(),
                    text = playlist.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(stringResource(R.string.songs, playlist.songs.count()))
            }

            IconButton(onClick = onDownloadPressed) {
                Icon(
                    Icons.Rounded.Download,
                    contentDescription = Icons.Rounded.Download.toString(),
                )
            }

        }

    }
}