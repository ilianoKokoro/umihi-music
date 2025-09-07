package ca.ilianokokoro.umihi.music.ui.screens.player.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R

@Composable
fun PlayPauseButton(
    isPlaying: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val playPauseShape by animateDpAsState(
        targetValue = if (isPlaying) 24.dp else 36.dp,
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "playPauseShape"
    )

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(playPauseShape))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center

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
