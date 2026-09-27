package ca.ilianokokoro.umihi.music.ui.screens.player.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.components.LoadingAnimation
import ca.ilianokokoro.umihi.music.ui.screens.player.LyricsState
import ca.ilianokokoro.umihi.music.ui.screens.player.Thumbnail

@Composable
fun TopPlayer(
    currentSong: Song?,
    isLyricsShown: Boolean,
    lyricsState: LyricsState,
    positionMs: () -> Long,
    modifier: Modifier
) {
    if (!isLyricsShown) {
        Thumbnail(
            href = currentSong?.thumbnailHref.toString(),
            modifier = modifier
        )
    } else {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = modifier
        ) {
            when (lyricsState) {
                is LyricsState.Loaded -> {
                    val lyrics = lyricsState.data
                    if (lyrics == null) {
                        Text(

                            stringResource(R.string.no_lyrics_found),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        LyricsDisplay(lyrics = lyrics, positionMs = positionMs)
                    }

                }

                LyricsState.Unloaded -> {
                    LoadingAnimation()
                }
            }
        }
    }
}