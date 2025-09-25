package ca.ilianokokoro.umihi.music.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.models.Playlist

@Composable
fun PlaylistInfo(playlist: Playlist, onDownloadPressed: () -> Unit, modifier: Modifier = Modifier) {
    val songsCount = playlist.songs.count()
    var animatedCount by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(songsCount) {
        animatedCount = songsCount
    }

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
                
                val alpha by animateFloatAsState(
                    targetValue = if (animatedCount == null || animatedCount == 0) 0f else 1f,
                    animationSpec = tween()
                )

                Text(
                    text = if (songsCount > 0) stringResource(R.string.songs, songsCount) else "",
                    modifier = Modifier.alpha(alpha)
                )
            }
        }
    }
}
