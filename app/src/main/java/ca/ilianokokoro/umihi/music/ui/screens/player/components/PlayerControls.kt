package ca.ilianokokoro.umihi.music.ui.screens.player.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.helpers.ComposeHelper
import ca.ilianokokoro.umihi.music.core.managers.PlayerManager
import ca.ilianokokoro.umihi.music.extensions.toTimeString
import ca.ilianokokoro.umihi.music.models.PlaybackAudioInfo
import ca.ilianokokoro.umihi.music.ui.screens.player.PlaybackProgress
import ca.ilianokokoro.umihi.music.ui.screens.player.components.PlaybackAudioInfoPill

@Composable
fun PlayerControls(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    isLoading: Boolean,
    progress: PlaybackProgress,
    onSeekPlayer: () -> Unit,
    onUpdateSeekBarHeldState: (isHeld: Boolean) -> Unit,
    onSeek: (location: Float) -> Unit,
    onOpenQueue: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenSpeedSelector: () -> Unit,
    playbackSpeed: Float,
    sleepTimerRemainingSeconds: Long?,
    audioInfo: PlaybackAudioInfo = PlaybackAudioInfo(),
) {
    val mainButtonsControlsInteractionSources =
        List(3) { ComposeHelper.rememberInteractionSource() }
    val actionButtonsControlsInteractionSources =
        List(4) { ComposeHelper.rememberInteractionSource() }

    val player by PlayerManager.controllerState.collectAsState()
    val repeatMode = ComposeHelper.rememberRepeatMode(player)

    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Slider(
            value = progress.position,
            valueRange = 0f..progress.duration,

            onValueChange = { newValue ->
                onUpdateSeekBarHeldState(true)
                onSeek(newValue)
            },

            onValueChangeFinished = {
                onSeekPlayer()
                onUpdateSeekBarHeldState(false)
            },

            modifier = Modifier.padding(top = 10.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = progress.position.toTimeString(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = progress.duration.toTimeString(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            PlaybackAudioInfoPill(
                info = audioInfo,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 58.dp),
            )
        }



        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ButtonGroup(
                overflowIndicator = {},
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                customItem(
                    {
                        FilledIconButton(
                            onClick = PlayerManager::skipToPrevious,
                            shapes = IconButtonDefaults.shapes(),
                            interactionSource = mainButtonsControlsInteractionSources[0],
                            modifier = Modifier
                                .padding(vertical = 20.dp)
                                .weight(2f)
                                .size(IconButtonDefaults.mediumContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide))
                                .animateWidth(interactionSource = mainButtonsControlsInteractionSources[0])
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipPrevious,
                                contentDescription = stringResource(R.string.previous),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    },
                    {}
                )

                customItem(
                    {

                        FilledIconToggleButton(
                            checked = isPlaying && !isLoading,
                            onCheckedChange = {
                                if (isPlaying) {
                                    PlayerManager.currentController?.pause()
                                } else {
                                    PlayerManager.currentController?.play()
                                }
                            },
                            shapes = IconButtonDefaults.toggleableShapes()
                                .copy(checkedShape = IconButtonDefaults.shapes().shape),
                            colors = IconButtonDefaults.filledIconToggleButtonColors(
                                checkedContainerColor = IconButtonDefaults.filledIconToggleButtonColors().checkedContainerColor,
                                checkedContentColor = IconButtonDefaults.filledIconToggleButtonColors().checkedContentColor,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            interactionSource = mainButtonsControlsInteractionSources[1],
                            modifier = Modifier
                                .weight(3f)
                                .size(IconButtonDefaults.largeContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide))
                                .animateWidth(interactionSource = mainButtonsControlsInteractionSources[1])
                        ) {
                            if (isLoading) {
                                CircularWavyProgressIndicator(
                                    modifier = Modifier.size(25.dp),
                                )
                            } else {
                                val icon = if (isPlaying) {
                                    Icons.Rounded.Pause
                                } else {
                                    Icons.Rounded.PlayArrow
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = icon.name,
                                    modifier = Modifier.size(50.dp)
                                )
                            }


                        }
                    },
                    {}
                )

                customItem(
                    {
                        FilledIconButton(
                            onClick = PlayerManager::skipToNext,
                            shapes = IconButtonDefaults.shapes(),
                            interactionSource = mainButtonsControlsInteractionSources[2],
                            modifier = Modifier
                                .padding(vertical = 20.dp)
                                .weight(2f)
                                .size(IconButtonDefaults.mediumContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide))
                                .animateWidth(interactionSource = mainButtonsControlsInteractionSources[2])
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = stringResource(R.string.next),
                                modifier = Modifier.size(30.dp)

                            )
                        }
                    },
                    {}
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.Bottom
        ) {
            ButtonGroup(
                overflowIndicator = {},
            ) {

                customItem(
                    {
                        val isNotDefaultSpeed = playbackSpeed != 1.0f

                        FilledIconToggleButton(
                            checked = isNotDefaultSpeed,
                            onCheckedChange = { onOpenSpeedSelector() },
                            shapes = IconButtonDefaults.toggleableShapes(),
                            colors = IconButtonDefaults.filledIconToggleButtonColors(
                                checkedContainerColor = IconButtonDefaults.filledIconToggleButtonColors().checkedContainerColor,
                                checkedContentColor = IconButtonDefaults.filledIconToggleButtonColors().checkedContentColor,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.animateWidth(
                                interactionSource = actionButtonsControlsInteractionSources[3]
                            ),
                            interactionSource = actionButtonsControlsInteractionSources[3],
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Speed,
                                contentDescription = stringResource(R.string.playback_speed),
                            )
                        }
                    },
                    {}
                )

                customItem(
                    {
                        val isTimerActive = sleepTimerRemainingSeconds != null

                        FilledIconToggleButton(
                            checked = isTimerActive,
                            onCheckedChange = { onOpenSleepTimer() },
                            shapes = IconButtonDefaults.toggleableShapes(),
                            colors = IconButtonDefaults.filledIconToggleButtonColors(
                                checkedContainerColor = IconButtonDefaults.filledIconToggleButtonColors().checkedContainerColor,
                                checkedContentColor = IconButtonDefaults.filledIconToggleButtonColors().checkedContentColor,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.animateWidth(
                                interactionSource = actionButtonsControlsInteractionSources[1]
                            ),
                            interactionSource = actionButtonsControlsInteractionSources[1],
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Timer,
                                contentDescription = stringResource(R.string.sleep_timer),
                            )
                        }
                    },
                    {}
                )
                customItem(
                    {
                        FilledIconButton(
                            onClick = onOpenQueue,
                            shapes = IconButtonDefaults.shapes(),
                            modifier = Modifier.animateWidth(interactionSource = actionButtonsControlsInteractionSources[0]),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            interactionSource = actionButtonsControlsInteractionSources[0],
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                                contentDescription = stringResource(R.string.queue),
                            )
                        }
                    },
                    {}
                )



                customItem(
                    {

                        FilledIconToggleButton(
                            checked = arrayOf(
                                Player.REPEAT_MODE_ALL,
                                Player.REPEAT_MODE_ONE
                            ).contains(
                                repeatMode
                            ),
                            onCheckedChange = {
                                PlayerManager.cycleRepeatMode()
                            },
                            shapes = IconButtonDefaults.toggleableShapes(),
                            colors = IconButtonDefaults.filledIconToggleButtonColors(
                                checkedContainerColor = IconButtonDefaults.filledIconToggleButtonColors().checkedContainerColor,
                                checkedContentColor = IconButtonDefaults.filledIconToggleButtonColors().checkedContentColor,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface

                            ),
                            modifier = Modifier.animateWidth(
                                interactionSource = actionButtonsControlsInteractionSources[2]
                            ),
                            interactionSource = actionButtonsControlsInteractionSources[2],
                        ) {
                            val icon = when (repeatMode) {
                                Player.REPEAT_MODE_OFF -> Icons.Rounded.Repeat
                                Player.REPEAT_MODE_ALL -> Icons.Rounded.Repeat
                                Player.REPEAT_MODE_ONE -> Icons.Rounded.RepeatOne
                                else -> Icons.Rounded.Repeat
                            }

                            Icon(
                                imageVector = icon,
                                contentDescription = icon.name
                            )
                        }


                    },
                    {}
                )
            }
        }


    }
}