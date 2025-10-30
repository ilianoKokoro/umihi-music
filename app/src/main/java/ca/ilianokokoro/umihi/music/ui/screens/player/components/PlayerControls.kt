package ca.ilianokokoro.umihi.music.ui.screens.player.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.core.helpers.ComposeHelper
import ca.ilianokokoro.umihi.music.extensions.toTimeString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerControls(
    isPlaying: Boolean,
    isLoading: Boolean,
    position: Float,
    duration: Float,
    onSeekPlayer: () -> Unit,
    onUpdateSeekBarHeldState: (isHeld: Boolean) -> Unit,
    onSeek: (location: Float) -> Unit,
    onPause: () -> Unit,
    onPlay: () -> Unit,
    onSeekToNext: () -> Unit,
    onSeekToPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    val controlsInteractionSources = List(3) { ComposeHelper.rememberInteractionSource() }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Slider
        Slider(
            value = position,
            valueRange = 0f..duration,

            onValueChange = { newValue ->
                onUpdateSeekBarHeldState(true)
                onSeek(newValue)
            },

            onValueChangeFinished = {
                onSeekPlayer()
                onUpdateSeekBarHeldState(false)
            },

            modifier = modifier.padding(top = 10.dp)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = position.toTimeString(),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = duration.toTimeString(),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            onClick = onSeekToPrevious,
                            shapes = IconButtonDefaults.shapes(),
                            interactionSource = controlsInteractionSources[0],
                            modifier = Modifier
                                .padding(vertical = 20.dp)
                                .weight(2f)
                                .size(IconButtonDefaults.mediumContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide))
                                .animateWidth(interactionSource = controlsInteractionSources[0])
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipPrevious,
                                contentDescription = Icons.Rounded.SkipPrevious.name,
                            )
                        }
                    },
                    {}
                )

                customItem(
                    {

                        FilledIconToggleButton(
                            enabled = !isLoading,
                            checked = isPlaying && !isLoading,
                            onCheckedChange = {
                                if (isLoading) {
                                    // Do nothing
                                } else if (isPlaying) {
                                    onPause()
                                } else {
                                    onPlay()
                                }
                            },
                            shapes = IconButtonDefaults.toggleableShapes()
                                .copy(checkedShape = IconButtonDefaults.shapes().shape),
                            interactionSource = controlsInteractionSources[1],
                            modifier = modifier
                                .weight(3f)
                                .size(IconButtonDefaults.largeContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide))
                                .animateWidth(interactionSource = controlsInteractionSources[1])
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
                                    modifier = modifier.size(30.dp)
                                )
                            }


                        }
                    },
                    {}
                )

                customItem(
                    {
                        FilledIconButton(
                            onClick = onSeekToNext,
                            shapes = IconButtonDefaults.shapes(),
                            interactionSource = controlsInteractionSources[2],
                            modifier = Modifier
                                .padding(vertical = 20.dp)
                                .weight(2f)
                                .size(IconButtonDefaults.mediumContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide))
                                .animateWidth(interactionSource = controlsInteractionSources[2])
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = Icons.Rounded.SkipNext.name,
                            )
                        }
                    },
                    {}
                )
            }
        }


    }
}