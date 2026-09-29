package ca.ilianokokoro.umihi.music.ui.screens.player.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.core.managers.PlayerManager
import ca.ilianokokoro.umihi.music.models.lyrics.Lyrics
import kotlin.math.roundToInt

@Composable
fun LyricsDisplay(
    lyrics: Lyrics,
    positionMs: () -> Long,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val currentLineIndex by remember(lyrics) {
        derivedStateOf { lyrics.indexOfCurrentLine(positionMs()) ?: 0 }
    }
    val density = LocalDensity.current
    val scrollOffset = with(density) { 100.dp.toPx().roundToInt() }

    LaunchedEffect(currentLineIndex) {
        if (currentLineIndex >= 0) {
            listState.animateScrollToItem(
                index = currentLineIndex,
                scrollOffset = -scrollOffset
            )
        }
    }


    LazyColumn(
        state = listState,
        modifier = modifier.padding(16.dp),
    ) {
        itemsIndexed(lyrics.displayLines) { index, line ->
            val isCurrent = index == currentLineIndex

            val (color, style) = if (isCurrent) {
                MaterialTheme.colorScheme.onPrimaryContainer to MaterialTheme.typography.headlineMediumEmphasized
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f) to MaterialTheme.typography.headlineMedium
            }

            Text(
                text = line.text,
                style = style,
                color = color,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        onClick = {
                            PlayerManager.currentController?.seekTo(line.timeMs)
                        }
                    )
                    .padding(vertical = 8.dp)
            )
        }
    }
}

