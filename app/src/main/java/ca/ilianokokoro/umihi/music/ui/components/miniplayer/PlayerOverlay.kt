package ca.ilianokokoro.umihi.music.ui.components.miniplayer

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.managers.PlayerManager
import ca.ilianokokoro.umihi.music.extensions.toSong
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.screens.player.PlayerScreen

enum class PlayerSheetState {
    Closed,
    MiniPlayer,
    FullPlayer
}

@Composable
fun PlayerOverlay(
    visible: Boolean,
    application: Application,
    modifier: Modifier = Modifier,
) {

    val player = PlayerManager.currentController
    var currentSong by remember { mutableStateOf(player?.currentMediaItem?.toSong()) }
    var songIsPlaying by remember(player) {
        mutableStateOf(player?.isPlaying)
    }
    var songIsLoading by remember(player) {
        mutableStateOf(player?.playbackState == Player.STATE_BUFFERING)
    }

    DisposableEffect(player) {
        currentSong = player?.currentMediaItem?.toSong()
        songIsPlaying = player?.isPlaying
        songIsLoading = player?.playbackState == Player.STATE_BUFFERING

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
        player?.addListener(listener)
        onDispose { player?.removeListener(listener) }
    }
    var mode by remember { mutableStateOf(PlayerSheetState.Closed) }

    LaunchedEffect(visible) {
        mode = if (visible) {
            PlayerSheetState.MiniPlayer
        } else {
            PlayerSheetState.Closed
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when (mode) {
            PlayerSheetState.Closed -> Unit

            PlayerSheetState.MiniPlayer -> {
                if (currentSong != null) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .height(Constants.Ui.MiniPlayer.HEIGHT)
                            .clickable { mode = PlayerSheetState.FullPlayer }
                    ) {
                        MiniPlayer(
                            currentSong = currentSong as Song,
                            onPlayPause = {
                                if (player?.isPlaying == true) {
                                    player.pause()
                                } else {
                                    player?.play()
                                }

                            },
                            onSkipNext = {
                                player?.seekToNext()
                            },
                            onSkipPrevious = {
                                player?.seekToPrevious()
                            },
                            isPlaying = songIsPlaying == true,
                            isLoading = songIsLoading
                        )
                    }
                }
            }

            PlayerSheetState.FullPlayer -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {}
                ) {
                    PlayerScreen(
                        modifier = Modifier.fillMaxSize(),
                        onBack = {
                            mode = PlayerSheetState.MiniPlayer
                        },
                        application = application
                    )
                }
            }
        }
    }
}