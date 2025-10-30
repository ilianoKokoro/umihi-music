package ca.ilianokokoro.umihi.music.ui.components.miniplayer

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.core.helpers.ComposeHelper
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.components.SquareImage

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    currentSong: Song,
    onClick: () -> Unit,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    isPlaying: Boolean,
    isLoading: Boolean
) {
    val controlsInteractionSources = List(3) { ComposeHelper.rememberInteractionSource() }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SquareImage(currentSong.thumbnailHref)

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
                    modifier = modifier.basicMarquee()
                )
                Text(
                    text = currentSong.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    modifier = modifier.basicMarquee()

                )
            }

            ButtonGroup(
                overflowIndicator = {},
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                customItem(
                    {
                        FilledIconButton(
                            onClick = onSkipPrevious,
                            shapes = IconButtonDefaults.shapes(),
                            interactionSource = controlsInteractionSources[0],
                            modifier = Modifier
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
                                } else {
                                    onPlayPause()
                                }
                            },
                            shapes = IconButtonDefaults.toggleableShapes()
                                .copy(checkedShape = IconButtonDefaults.shapes().shape),
                            interactionSource = controlsInteractionSources[1],
                            modifier = modifier
                                .animateWidth(interactionSource = controlsInteractionSources[1])
                        ) {
                            if (isLoading) {
                                CircularWavyProgressIndicator(
                                    modifier = Modifier.size(15.dp),
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
                                    modifier = modifier.size(20.dp)
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
                            shapes = IconButtonDefaults.shapes(),
                            interactionSource = controlsInteractionSources[2],
                            modifier = Modifier
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
