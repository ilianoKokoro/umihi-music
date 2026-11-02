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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.helpers.ComposeHelper
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.components.SquareImage
import ca.ilianokokoro.umihi.music.ui.icons.pause
import ca.ilianokokoro.umihi.music.ui.icons.play_arrow
import ca.ilianokokoro.umihi.music.ui.icons.skip_next
import ca.ilianokokoro.umihi.music.ui.icons.skip_previous

@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    currentSong: Song,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    isPlaying: Boolean,
    isLoading: Boolean,
) {
    val controlsInteractionSources = List(3) { ComposeHelper.rememberInteractionSource() }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SquareImage(
                uri = currentSong.thumbnailPath ?: currentSong.thumbnailHref,
                modifier = Modifier.size(50.dp),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = currentSong.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee()
                )
                Text(
                    text = currentSong.artists,
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
                            onClick = onSkipPrevious,
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
                            enabled = !isLoading,
                            checked = isPlaying && !isLoading,
                            onCheckedChange = {
                                if (!isLoading) {
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
                            if (isLoading) {
                                CircularWavyProgressIndicator(
                                    modifier = Modifier.size(15.dp),
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
                            onClick = onSkipNext,
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
}
