package ca.ilianokokoro.umihi.music.ui.screens.player.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeMute
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.helpers.ComposeHelper
import ca.ilianokokoro.umihi.music.core.managers.PlayerManager
import ca.ilianokokoro.umihi.music.ui.components.materialu.dropdown.MaterialUDropdownItem


@Composable
fun PlayerActionButtons(
    modifier: Modifier = Modifier,
    isLyricsShown: Boolean,
    isMixActive: Boolean,
    onToggleMix: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenVolume: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onToggleLyrics: () -> Unit,
    onOpenSpeedSelector: () -> Unit,
    playbackSpeed: Float,
    sleepTimerRemainingSeconds: Long?,
) {
    val actionButtonsControlsInteractionSources =
        List(8) { ComposeHelper.rememberInteractionSource() }
    val player by PlayerManager.controllerState.collectAsState()
    val repeatMode = ComposeHelper.rememberRepeatMode(player)
    val appVolume by PlayerManager.appVolume.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val buttonSize = 52.dp
        val iconSize = 24.dp

        Row {
            ButtonGroup(
                overflowIndicator = { menuState ->
                    FilledIconButton(
                        onClick = {
                            if (menuState.isShowing) {
                                menuState.dismiss()
                            } else {
                                menuState.show()
                            }
                        },
                        shapes = IconButtonDefaults.shapes(
                            shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                            pressedShape = ButtonGroupDefaults.connectedTrailingButtonPressShape,
                        ),
                        modifier = Modifier.size(buttonSize),
                    ) {
                        Icon(
                            Icons.Rounded.MoreVert,
                            contentDescription = stringResource(R.string.more),
                            modifier = Modifier.size(iconSize)
                        )
                    }
                },
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
            ) {
                // Volume — START segment
                customItem(
                    buttonGroupContent = {
                        val isBoosted = appVolume > 100
                        val isMuted = appVolume <= 0
                        val activeColors = IconButtonDefaults.filledIconToggleButtonColors()
                        val volumeIcon = when {
                            isMuted -> Icons.AutoMirrored.Rounded.VolumeMute
                            appVolume <= 50 -> Icons.AutoMirrored.Rounded.VolumeDown
                            else -> Icons.AutoMirrored.Rounded.VolumeUp
                        }

                        FilledIconButton(
                            onClick = onOpenVolume,
                            shapes = IconButtonDefaults.shapes(
                                shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                                pressedShape = ButtonGroupDefaults.connectedLeadingButtonPressShape,
                            ),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (isBoosted) activeColors.checkedContainerColor else MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = if (isBoosted) activeColors.checkedContentColor else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .size(buttonSize)
                                .animateWidth(interactionSource = actionButtonsControlsInteractionSources[0]),
                            interactionSource = actionButtonsControlsInteractionSources[0],
                        ) {
                            Icon(
                                imageVector = volumeIcon,
                                contentDescription = stringResource(R.string.volume),
                                modifier = Modifier.size(iconSize)
                            )
                        }
                    },
                    menuContent = { menuState ->
                        val isMuted = appVolume <= 0
                        val volumeIcon = when {
                            isMuted -> Icons.AutoMirrored.Rounded.VolumeMute
                            appVolume <= 50 -> Icons.AutoMirrored.Rounded.VolumeDown
                            else -> Icons.AutoMirrored.Rounded.VolumeUp
                        }
                        MaterialUDropdownItem(
                            text = stringResource(R.string.volume),
                            leadingIcon = volumeIcon,
                            onClick = {
                                onOpenVolume()
                                menuState.dismiss()
                            }
                        )
                    }
                )

                // Repeat — MIDDLE segment
                customItem(
                    buttonGroupContent = {
                        FilledIconToggleButton(
                            checked = repeatMode == Player.REPEAT_MODE_ALL || repeatMode == Player.REPEAT_MODE_ONE,
                            onCheckedChange = { PlayerManager.cycleRepeatMode() },
                            shapes = IconButtonDefaults.toggleableShapes(
                                shape = ButtonGroupDefaults.connectedMiddleButtonPressShape,
                                pressedShape = ButtonGroupDefaults.connectedMiddleButtonPressShape,
                                checkedShape = ButtonGroupDefaults.connectedMiddleButtonPressShape,
                            ),
                            colors = IconButtonDefaults.filledIconToggleButtonColors(
                                checkedContainerColor = IconButtonDefaults.filledIconToggleButtonColors().checkedContainerColor,
                                checkedContentColor = IconButtonDefaults.filledIconToggleButtonColors().checkedContentColor,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .size(buttonSize)
                                .animateWidth(interactionSource = actionButtonsControlsInteractionSources[1]),
                            interactionSource = actionButtonsControlsInteractionSources[1],
                        ) {
                            val repeatDescription = stringResource(
                                when (repeatMode) {
                                    Player.REPEAT_MODE_ONE -> R.string.repeat_one
                                    Player.REPEAT_MODE_ALL -> R.string.repeat_all
                                    else -> R.string.repeat_off
                                }
                            )
                            Icon(
                                imageVector = when (repeatMode) {
                                    Player.REPEAT_MODE_ONE -> Icons.Rounded.RepeatOne
                                    else -> Icons.Rounded.Repeat
                                },
                                contentDescription = repeatDescription,
                                modifier = Modifier.size(iconSize)
                            )
                        }
                    },
                    menuContent = { menuState ->
                        MaterialUDropdownItem(
                            text = stringResource(
                                when (repeatMode) {
                                    Player.REPEAT_MODE_ONE -> R.string.repeat_one
                                    Player.REPEAT_MODE_ALL -> R.string.repeat_all
                                    else -> R.string.repeat_off
                                }
                            ),
                            leadingIcon = when (repeatMode) {
                                Player.REPEAT_MODE_ONE -> Icons.Rounded.RepeatOne
                                else -> Icons.Rounded.Repeat
                            },
                            onClick = {
                                PlayerManager.cycleRepeatMode()
                                menuState.dismiss()
                            }
                        )
                    }
                )

                // Shuffle — MIDDLE segment
                customItem(
                    buttonGroupContent = {
                        FilledIconButton(
                            onClick = { PlayerManager.shuffleQueue() },
                            shapes = IconButtonDefaults.shapes(
                                shape = ButtonGroupDefaults.connectedMiddleButtonPressShape,
                                pressedShape = ButtonGroupDefaults.connectedMiddleButtonPressShape,
                            ),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .size(buttonSize)
                                .animateWidth(interactionSource = actionButtonsControlsInteractionSources[2]),
                            interactionSource = actionButtonsControlsInteractionSources[2],
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = stringResource(R.string.shuffle),
                                modifier = Modifier.size(iconSize)
                            )
                        }
                    },
                    menuContent = { menuState ->
                        MaterialUDropdownItem(
                            text = stringResource(R.string.shuffle),
                            leadingIcon = Icons.Rounded.Shuffle,
                            onClick = {
                                PlayerManager.shuffleQueue()
                                menuState.dismiss()
                            }
                        )
                    }
                )

                // Mix — MIDDLE segment
                customItem(
                    buttonGroupContent = {
                        FilledIconToggleButton(
                            checked = isMixActive,
                            onCheckedChange = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                                onToggleMix()
                            },
                            shapes = IconButtonDefaults.toggleableShapes(
                                shape = ButtonGroupDefaults.connectedMiddleButtonPressShape,
                                pressedShape = ButtonGroupDefaults.connectedMiddleButtonPressShape,
                                checkedShape = ButtonGroupDefaults.connectedMiddleButtonPressShape,
                            ),
                            colors = IconButtonDefaults.filledIconToggleButtonColors(
                                checkedContainerColor = IconButtonDefaults.filledIconToggleButtonColors().checkedContainerColor,
                                checkedContentColor = IconButtonDefaults.filledIconToggleButtonColors().checkedContentColor,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .size(buttonSize)
                                .animateWidth(interactionSource = actionButtonsControlsInteractionSources[3]),
                            interactionSource = actionButtonsControlsInteractionSources[3],
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Sensors,
                                contentDescription = stringResource(R.string.mix),
                                modifier = Modifier.size(iconSize)
                            )
                        }
                    },
                    menuContent = { menuState ->
                        MaterialUDropdownItem(
                            text = stringResource(R.string.mix),
                            leadingIcon = Icons.Rounded.Sensors,
                            onClick = {
                                onToggleMix()
                                menuState.dismiss()
                            }
                        )
                    }
                )

                // Queue — MIDDLE segment
                customItem(
                    buttonGroupContent = {
                        FilledIconButton(
                            onClick = onOpenQueue,
                            shapes = IconButtonDefaults.shapes(
                                shape = ButtonGroupDefaults.connectedMiddleButtonPressShape,
                                pressedShape = ButtonGroupDefaults.connectedMiddleButtonPressShape,
                            ),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .size(buttonSize)
                                .animateWidth(interactionSource = actionButtonsControlsInteractionSources[4]),
                            interactionSource = actionButtonsControlsInteractionSources[4],
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                                contentDescription = stringResource(R.string.queue),
                                modifier = Modifier.size(iconSize)
                            )
                        }
                    },
                    menuContent = { menuState ->
                        MaterialUDropdownItem(
                            text = stringResource(R.string.queue),
                            leadingIcon = Icons.AutoMirrored.Rounded.QueueMusic,
                            onClick = {
                                onOpenQueue()
                                menuState.dismiss()
                            }
                        )
                    }
                )

                // Lyrics — MIDDLE segment
                customItem(
                    buttonGroupContent = {
                        FilledIconToggleButton(
                            checked = isLyricsShown,
                            onCheckedChange = { onToggleLyrics() },
                            shapes = IconButtonDefaults.toggleableShapes(
                                shape = ButtonGroupDefaults.connectedMiddleButtonPressShape,
                                pressedShape = ButtonGroupDefaults.connectedMiddleButtonPressShape,
                                checkedShape = ButtonGroupDefaults.connectedMiddleButtonPressShape,
                            ),
                            colors = IconButtonDefaults.filledIconToggleButtonColors(
                                checkedContainerColor = IconButtonDefaults.filledIconToggleButtonColors().checkedContainerColor,
                                checkedContentColor = IconButtonDefaults.filledIconToggleButtonColors().checkedContentColor,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .size(buttonSize)
                                .animateWidth(interactionSource = actionButtonsControlsInteractionSources[5]),
                            interactionSource = actionButtonsControlsInteractionSources[5],
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lyrics,
                                contentDescription = stringResource(R.string.lyrics),
                                modifier = Modifier.size(iconSize)
                            )
                        }
                    },
                    menuContent = { menuState ->
                        MaterialUDropdownItem(
                            text = stringResource(R.string.lyrics),
                            leadingIcon = Icons.Rounded.Lyrics,
                            onClick = {
                                onToggleLyrics()
                                menuState.dismiss()
                            }
                        )
                    }
                )

                // Sleep Timer — MIDDLE segment
                customItem(
                    buttonGroupContent = {
                        val isTimerActive = sleepTimerRemainingSeconds != null
                        FilledIconToggleButton(
                            checked = isTimerActive,
                            onCheckedChange = { onOpenSleepTimer() },
                            shapes = IconButtonDefaults.toggleableShapes(
                                shape = ButtonGroupDefaults.connectedMiddleButtonPressShape,
                                pressedShape = ButtonGroupDefaults.connectedMiddleButtonPressShape,
                                checkedShape = ButtonGroupDefaults.connectedMiddleButtonPressShape,
                            ),
                            colors = IconButtonDefaults.filledIconToggleButtonColors(
                                checkedContainerColor = IconButtonDefaults.filledIconToggleButtonColors().checkedContainerColor,
                                checkedContentColor = IconButtonDefaults.filledIconToggleButtonColors().checkedContentColor,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .size(buttonSize)
                                .animateWidth(interactionSource = actionButtonsControlsInteractionSources[6]),
                            interactionSource = actionButtonsControlsInteractionSources[6],
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Timer,
                                contentDescription = stringResource(R.string.sleep_timer),
                                modifier = Modifier.size(iconSize)
                            )
                        }
                    },
                    menuContent = { menuState ->
                        MaterialUDropdownItem(
                            text = stringResource(R.string.sleep_timer),
                            leadingIcon = Icons.Rounded.Timer,
                            onClick = {
                                onOpenSleepTimer()
                                menuState.dismiss()
                            }
                        )
                    }
                )

                // Speed — END segment
                customItem(
                    buttonGroupContent = {
                        val isNotDefaultSpeed = playbackSpeed != 1.0f
                        FilledIconToggleButton(
                            checked = isNotDefaultSpeed,
                            onCheckedChange = { onOpenSpeedSelector() },
                            shapes = IconButtonDefaults.toggleableShapes(
                                shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                                pressedShape = ButtonGroupDefaults.connectedTrailingButtonPressShape,
                                checkedShape = ButtonGroupDefaults.connectedTrailingButtonShape,
                            ),
                            colors = IconButtonDefaults.filledIconToggleButtonColors(
                                checkedContainerColor = IconButtonDefaults.filledIconToggleButtonColors().checkedContainerColor,
                                checkedContentColor = IconButtonDefaults.filledIconToggleButtonColors().checkedContentColor,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .size(buttonSize)
                                .animateWidth(interactionSource = actionButtonsControlsInteractionSources[7]),
                            interactionSource = actionButtonsControlsInteractionSources[7],
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Speed,
                                contentDescription = stringResource(R.string.playback_speed),
                                modifier = Modifier.size(iconSize)
                            )
                        }
                    },
                    menuContent = { menuState ->
                        MaterialUDropdownItem(
                            text = stringResource(R.string.playback_speed),
                            leadingIcon = Icons.Rounded.Speed,
                            onClick = {
                                onOpenSpeedSelector()
                                menuState.dismiss()
                            }
                        )
                    }
                )
            }
        }
    }
}
