package ca.ilianokokoro.umihi.music.ui.screens.player.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.helpers.ComposeHelper
import ca.ilianokokoro.umihi.music.core.managers.PlayerManager
import ca.ilianokokoro.umihi.music.extensions.toTimeString
import ca.ilianokokoro.umihi.music.ui.icons.pause
import ca.ilianokokoro.umihi.music.ui.icons.play_arrow
import ca.ilianokokoro.umihi.music.ui.icons.skip_next
import ca.ilianokokoro.umihi.music.ui.icons.skip_previous
import ca.ilianokokoro.umihi.music.ui.screens.player.PlaybackProgress
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PlayerControls(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    isLoading: Boolean,
    progress: StateFlow<PlaybackProgress>,
    onSeekPlayer: () -> Unit,
    onUpdateSeekBarHeldState: (isHeld: Boolean) -> Unit,
    onSeek: (location: Float) -> Unit,
) {
    val mainButtonsControlsInteractionSources =
        List(3) { ComposeHelper.rememberInteractionSource() }

    val hapticFeedback = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        SeekBar(
            progress = progress,
            onSeek = onSeek,
            onSeekPlayer = onSeekPlayer,
            onUpdateSeekBarHeldState = onUpdateSeekBarHeldState,
        )

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
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                                PlayerManager.skipToPrevious()
                            },
                            shapes = IconButtonDefaults.shapes(),
                            interactionSource = mainButtonsControlsInteractionSources[0],
                            modifier = Modifier
                                .padding(vertical = 20.dp)
                                .weight(2f)
                                .size(IconButtonDefaults.mediumContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide))
                                .animateWidth(interactionSource = mainButtonsControlsInteractionSources[0])
                        ) {
                            Icon(
                                imageVector = skip_previous,
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
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                                if (isPlaying) {
                                    PlayerManager.currentController?.pause()
                                } else {
                                    PlayerManager.currentController?.play()
                                }
                            },
                            shapes = IconButtonDefaults.toggleableShapes(),
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
                                    pause
                                } else {
                                    play_arrow
                                }

                                val text = if (isPlaying) {
                                    R.string.pause
                                } else {
                                    R.string.play
                                }

                                Icon(
                                    imageVector = icon,
                                    contentDescription = stringResource(text),
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
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                                PlayerManager.skipToNext()
                            },
                            shapes = IconButtonDefaults.shapes(),
                            interactionSource = mainButtonsControlsInteractionSources[2],
                            modifier = Modifier
                                .padding(vertical = 20.dp)
                                .weight(2f)
                                .size(IconButtonDefaults.mediumContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide))
                                .animateWidth(interactionSource = mainButtonsControlsInteractionSources[2])
                        ) {
                            Icon(
                                imageVector = skip_next,
                                contentDescription = stringResource(R.string.next),
                                modifier = Modifier.size(30.dp)

                            )
                        }
                    },
                    {}
                )
            }
        }
    }
}


@Composable
private fun SeekBar(
    progress: StateFlow<PlaybackProgress>,
    onSeek: (Float) -> Unit,
    onSeekPlayer: () -> Unit,
    onUpdateSeekBarHeldState: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val progressValue by progress.collectAsStateWithLifecycle()
    val audioInfo by PlayerManager.audioInfo.collectAsStateWithLifecycle()

    val sliderState = remember(progressValue.duration) {
        SliderState(
            value = progressValue.position,
            trackRange = 0f..progressValue.duration,
        )
    }

    LaunchedEffect(progressValue.position, sliderState) {
        if (sliderState.value != progressValue.position) {
            sliderState.value = progressValue.position
        }
    }

    Column(modifier = modifier) {
        Slider(
            state = sliderState,
            onValueChange = { newValue ->
                onUpdateSeekBarHeldState(true)
                sliderState.value = newValue
                onSeek(newValue)
            },
            onValueChangeFinished = {
                onSeekPlayer()
                onUpdateSeekBarHeldState(false)
            },
            modifier = Modifier.padding(top = 10.dp),
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
                    text = progressValue.position.toTimeString(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = progressValue.duration.toTimeString(),
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
    }
}
