package ca.ilianokokoro.umihi.music.ui.screens.player.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R

@Composable
fun PlayPauseButton(
    isPlaying: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    FloatingActionButton(
        // TODO : Revamp the whole button to be shape animated ? (Both the icon and contour)
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(35.dp),
                    strokeWidth = 5.dp
                )
            } else {
                AnimatedContent(
                    targetState = isPlaying,
                    label = "PlayPauseAnimation"
                ) { targetIsPlaying ->
                    if (targetIsPlaying) {
                        Icon(
                            imageVector = Icons.Rounded.Pause,
                            contentDescription = stringResource(R.string.pause),
                            modifier = Modifier.size(35.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = stringResource(R.string.play),
                            modifier = Modifier.size(35.dp)
                        )
                    }
                }
            }
        }
    }
}