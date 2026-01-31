package ca.ilianokokoro.umihi.music.ui.components.miniplayer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import ca.ilianokokoro.umihi.music.extensions.toSong


@Composable
fun MiniPlayerWrapper(
    modifier: Modifier = Modifier,
    player: Player,
    onMiniPlayerPressed: () -> Unit,
    isPlayerOpened: Boolean
) {
    var currentSong by remember { mutableStateOf(player.currentMediaItem?.toSong()) }
    var songIsPlaying by remember { mutableStateOf(false) }
    var songIsLoading by remember { mutableStateOf(true) }
    val insets = WindowInsets.navigationBars.asPaddingValues()
    val bottomInset = with(LocalDensity.current) { insets.calculateBottomPadding().roundToPx() }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentSong = mediaItem?.toSong()


            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                songIsPlaying = isPlaying
            }


            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        songIsLoading = true
                    }

                    Player.STATE_READY -> {
                        songIsLoading = false
                    }

                    else -> {
                    }
                }
            }

        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    AnimatedVisibility(
        visible = currentSong != null && !isPlayerOpened,
        enter = slideInVertically(initialOffsetY = { it + bottomInset }),
        exit = slideOutVertically(targetOffsetY = { it + bottomInset }),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .height(70.dp)
    ) {
        MiniPlayer(
            currentSong = currentSong!!,
            onClick = onMiniPlayerPressed,
            onPlayPause = {
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            },
            onSkipNext = {
                player.seekToNext()
            },
            onSkipPrevious = {
                player.seekToPrevious()
            },
            isPlaying = songIsPlaying,
            isLoading = songIsLoading
        )
    }
}
