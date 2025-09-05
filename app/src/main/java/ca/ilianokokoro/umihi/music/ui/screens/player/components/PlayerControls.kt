package ca.ilianokokoro.umihi.music.ui.screens.player.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.extensions.toTimeString

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

            modifier = modifier.padding(vertical = 10.dp)
        )

        // TODO : Reduce spacing

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier.fillMaxWidth()
        ) {
            Text(position.toTimeString(), fontWeight = FontWeight.Bold)
            Text(duration.toTimeString(), fontWeight = FontWeight.Bold)
        }

        // Player controls row
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onSeekToPrevious) {
                Icon(
                    Icons.Rounded.SkipPrevious,
                    contentDescription = "Previous",
                    modifier = modifier.size(45.dp)
                )
            }

            PlayPauseButton(
                isPlaying = isPlaying,
                isLoading = isLoading,
                onClick = {
                    if (isPlaying) {
                        onPause()
                    } else {
                        onPlay()
                    }
                }
            )

            IconButton(onClick = onSeekToNext) {
                Icon(
                    Icons.Rounded.SkipNext,
                    contentDescription = "Next",
                    modifier = modifier.size(45.dp)
                )
            }
        }
    }
}

