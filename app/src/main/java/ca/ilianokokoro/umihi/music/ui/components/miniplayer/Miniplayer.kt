package ca.ilianokokoro.umihi.music.ui.components.miniplayer

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.helpers.ComposeHelper
import ca.ilianokokoro.umihi.music.core.managers.PlayerManager
import ca.ilianokokoro.umihi.music.extensions.toSong
import ca.ilianokokoro.umihi.music.ui.components.SquareImage
import ca.ilianokokoro.umihi.music.ui.icons.pause
import ca.ilianokokoro.umihi.music.ui.icons.play_arrow
import ca.ilianokokoro.umihi.music.ui.icons.skip_next
import ca.ilianokokoro.umihi.music.ui.icons.skip_previous

@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
) {
    val controlsInteractionSources = List(3) { ComposeHelper.rememberInteractionSource() }
    val player by PlayerManager.controllerState.collectAsStateWithLifecycle()
    var currentSong by remember { mutableStateOf(player?.currentMediaItem?.toSong()) }

    var songIsPlaying by remember(player) {
        mutableStateOf(player?.isPlaying)
    }
    var songIsLoading by remember(player) {
        mutableStateOf(player?.playbackState == Player.STATE_BUFFERING)
    }

    fun onPlayPause() {
        if (player?.isPlaying == true) {
            player?.pause()
        } else {
            player?.play()
        }
    }

    fun onSkipNext() {
        player?.seekToNext()
    }

    fun onSkipPrevious() {
        player?.seekToPrevious()
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

                    else -> {}
                }
            }
        }
        player?.addListener(listener)
        onDispose { player?.removeListener(listener) }
    }


    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SquareImage(
            uri = currentSong?.thumbnailPath ?: currentSong?.thumbnailHref ?: "",
            modifier = Modifier.size(50.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = currentSong?.title ?: "",
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.basicMarquee()
            )
            Text(
                text = currentSong?.artists ?: "",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.basicMarquee()
            )
        }

        ButtonGroup(
            overflowIndicator = {},
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
        ) {
            customItem(
                {
                    FilledIconButton(
                        onClick = { onSkipPrevious() },
                        shapes = IconButtonDefaults.shapes(
                            shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                            pressedShape = ButtonGroupDefaults.connectedLeadingButtonPressShape,
                        ),
                        interactionSource = controlsInteractionSources[0],
                        modifier = Modifier.animateWidth(
                            interactionSource = controlsInteractionSources[0]
                        )
                    ) {
                        Icon(
                            imageVector = skip_previous,
                            contentDescription = stringResource(R.string.previous),
                        )
                    }
                },
                {}
            )

            customItem(
                {
                    FilledIconToggleButton(
                        enabled = !songIsLoading,
                        checked = songIsPlaying == true && !songIsLoading,
                        onCheckedChange = {
                            if (!songIsLoading) {
                                onPlayPause()
                            }
                        },
                        shapes = IconButtonDefaults.toggleableShapes(
                            shape = ButtonGroupDefaults.connectedMiddleButtonPressShape,
                            pressedShape = ButtonGroupDefaults.connectedMiddleButtonPressShape,
                            checkedShape = ButtonGroupDefaults.connectedMiddleButtonPressShape,
                        ),
                        interactionSource = controlsInteractionSources[1],
                        modifier = Modifier.animateWidth(
                            interactionSource = controlsInteractionSources[1]
                        )
                    ) {
                        if (songIsLoading) {
                            CircularWavyProgressIndicator(
                                modifier = Modifier.size(15.dp),
                            )
                        } else {
                            val icon = if (songIsPlaying == true) {
                                pause
                            } else {
                                play_arrow
                            }

                            val text = if (songIsPlaying == true) {
                                R.string.pause
                            } else {
                                R.string.play
                            }

                            Icon(
                                imageVector = icon,
                                contentDescription = stringResource(text),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                {}
            )

            customItem(
                {
                    FilledIconButton(
                        onClick = { onSkipNext() },
                        shapes = IconButtonDefaults.shapes(
                            shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                            pressedShape = ButtonGroupDefaults.connectedTrailingButtonPressShape,
                        ),
                        interactionSource = controlsInteractionSources[2],
                        modifier = Modifier.animateWidth(
                            interactionSource = controlsInteractionSources[2]
                        )
                    ) {
                        Icon(
                            imageVector = skip_next,
                            contentDescription = stringResource(R.string.next)
                        )
                    }
                },
                {}
            )
        }
    }

}
