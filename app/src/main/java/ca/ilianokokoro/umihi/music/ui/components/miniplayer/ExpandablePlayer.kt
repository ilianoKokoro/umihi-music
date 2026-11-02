package ca.ilianokokoro.umihi.music.ui.components.miniplayer

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateToWithDecay
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.screens.player.PlayerScreen
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class PlayerAnchor { Expanded, Collapsed, Dismissed }

private const val EXPAND_POSITIONAL_THRESHOLD = 0.5f
private const val DISMISS_POSITIONAL_THRESHOLD = 0.4f

@Composable
fun ExpandablePlayer(
    modifier: Modifier = Modifier,
    bottomPadding: Dp,
    onClose: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as Application

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()

        val miniPx = with(density) { Constants.Ui.MiniPlayer.HEIGHT.toPx() }

        val bottomPx = with(density) { bottomPadding.toPx() }

        val collapsedPx = constraints.maxHeight - miniPx - bottomPx
        val dismissedPx = collapsedPx + (miniPx + bottomPx) * 1.5f
        val flingPx = with(density) { 125.dp.toPx() }

        val state = remember(collapsedPx, dismissedPx) {
            AnchoredDraggableState(
                initialValue = PlayerAnchor.Collapsed,
                anchors = DraggableAnchors {
                    PlayerAnchor.Expanded at 0f
                    PlayerAnchor.Collapsed at collapsedPx
                    PlayerAnchor.Dismissed at dismissedPx
                },
            )
        }

        // 0 = mini, 1 = full
        fun progress() = (1f - state.requireOffset() / collapsedPx).coerceIn(0f, 1f)

        fun settle(target: PlayerAnchor, velocity: Float = 0f) {
            scope.launch { state.animateToWithDecay(target, velocity) }
        }

        // Holds the anchoredDrag mutex while the finger is down so a running settle animation is interrupted.
        val grab = remember { mutableStateOf<Job?>(null) }

        val dragState = rememberDraggableState { delta -> state.dispatchRawDelta(delta) }

        LaunchedEffect(state.settledValue) {
            if (state.settledValue == PlayerAnchor.Dismissed) {
                onClose()
            }
        }

        BackHandler(enabled = state.settledValue == PlayerAnchor.Expanded) {
            settle(PlayerAnchor.Collapsed)
        }

        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, state.requireOffset().roundToInt()) }
                .draggable(
                    state = dragState,
                    orientation = Orientation.Vertical,
                    onDragStarted = {
                        grab.value = scope.launch { state.anchoredDrag { awaitCancellation() } }
                    },
                    onDragStopped = { velocity ->
                        grab.value?.cancel()
                        grab.value = null

                        val offset = state.requireOffset()
                        val target = when {
                            // Flick up: next anchor above
                            velocity <= -flingPx ->
                                if (offset > collapsedPx) PlayerAnchor.Collapsed else PlayerAnchor.Expanded

                            // Flick down: next anchor below
                            velocity >= flingPx ->
                                if (offset < collapsedPx) PlayerAnchor.Collapsed else PlayerAnchor.Dismissed

                            // Slow release: position decides
                            offset >= collapsedPx ->
                                if (offset - collapsedPx >= (dismissedPx - collapsedPx) * DISMISS_POSITIONAL_THRESHOLD)
                                    PlayerAnchor.Dismissed else PlayerAnchor.Collapsed

                            else ->
                                if (offset <= collapsedPx * EXPAND_POSITIONAL_THRESHOLD)
                                    PlayerAnchor.Expanded else PlayerAnchor.Collapsed
                        }
                        settle(target, velocity)
                    }
                )
                .clickable(enabled = state.settledValue == PlayerAnchor.Collapsed) {
                    settle(PlayerAnchor.Expanded)
                },
            shape = RoundedCornerShape(
                topStart = lerp(12.dp, 0.dp, progress()),
                topEnd = lerp(12.dp, 0.dp, progress()),
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(Modifier.fillMaxSize()) {
                // Mini layer: pinned to the top of the panel, fades out first
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(Constants.Ui.MiniPlayer.HEIGHT)
                        .graphicsLayer { alpha = (1f - progress() * 4f).coerceIn(0f, 1f) }
                ) {
                    MiniPlayer(// TEMP
                        onPlayPause = {},
                        onSkipNext = {},
                        onSkipPrevious = {},
                        isPlaying = false,
                        isLoading = false,
                        currentSong = Song(youtubeId = "")
                    )
                }

                // Full layer: fades in late
                Box(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = ((progress() - 0.2f) / 0.8f).coerceIn(0f, 1f) }
                ) {
                    PlayerScreen(onBack = onClose, application = app)
                }
            }
        }
    }
}