package ca.ilianokokoro.umihi.music.ui.components.miniplayer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.animateToWithDecay
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChangeIgnoreConsumed
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.models.Song
import kotlin.math.abs
import kotlinx.coroutines.channels.Channel

private sealed interface PlayerDragEvent {
    data class Move(val rawOffset: Float, val startOffset: Float) : PlayerDragEvent

    data class End(val velocityY: Float) : PlayerDragEvent

    data class Target(val value: Float) : PlayerDragEvent
}

private enum class DragZone { Strip, Full }

@Composable
fun ExpandingPlayer(
    modifier: Modifier = Modifier,
    currentSong: Song,
    isPlaying: Boolean,
    isLoading: Boolean,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onDismiss: () -> Unit,
    bottomPadding: Dp,
    expandRequest: Int,
    fullPlayer: @Composable (collapse: () -> Unit) -> Unit,
) {
    val density = LocalDensity.current
    val touchSlop = LocalViewConfiguration.current.touchSlop

    val miniPlayerHeightPx = with(density) { Constants.Ui.MiniPlayer.HEIGHT.toPx() }
    val stripVerticalPaddingPx = with(density) { 8.dp.toPx() }
    val bottomPaddingPx = with(density) { bottomPadding.toPx() }
    val dismissTravel = miniPlayerHeightPx * 1.5f
    val minFlingVelocity = with(density) { 125.dp.toPx() }

    val dragEvents = remember { Channel<PlayerDragEvent>(Channel.UNLIMITED) }
    var dragOwner by remember { mutableStateOf<DragZone?>(null) }

    var internalExpandRequest by remember { mutableIntStateOf(0) }
    val totalExpandRequest = internalExpandRequest + expandRequest

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
    ) {
        val collapsedTopY = with(density) {
            maxHeight.toPx() - miniPlayerHeightPx - bottomPaddingPx - stripVerticalPaddingPx
        }.coerceAtLeast(0f)
        val dismissOffset = collapsedTopY + dismissTravel
        val currentCollapsedTopY by rememberUpdatedState(collapsedTopY)
        val currentDismissOffset by rememberUpdatedState(dismissOffset)

        val state = remember {
            AnchoredDraggableState(
                initialValue = collapsedTopY,
                anchors = DraggableAnchors {
                    0f at 0f
                    collapsedTopY at collapsedTopY
                    dismissOffset at dismissOffset
                },
            )
        }

        SideEffect {
            state.updateAnchors(
                DraggableAnchors {
                    0f at 0f
                    collapsedTopY at collapsedTopY
                    dismissOffset at dismissOffset
                }
            )
        }

        val progress = ((collapsedTopY - state.requireOffset()) / collapsedTopY)
            .coerceIn(0f, 1f)

        val onCurrentDismiss by rememberUpdatedState(onDismiss)

        LaunchedEffect(state.settledValue, dismissOffset) {
            if (state.settledValue == dismissOffset) {
                onCurrentDismiss()
            }
        }

        LaunchedEffect(totalExpandRequest) {
            if (totalExpandRequest > 0) {
                dragEvents.trySend(PlayerDragEvent.Target(0f))
            }
        }

        LaunchedEffect(Unit) {
            while (true) {
                var releaseVelocity = 0f
                var targetOverride: Float? = null

                state.anchoredDrag {
                    for (event in dragEvents) {
                        when (event) {
is PlayerDragEvent.Move -> {
                        val appliedDelta = if (event.rawOffset >= 0f) {
                            event.rawOffset - touchSlop
                        } else {
                            event.rawOffset + touchSlop
                        }
                        dragTo(
                            (event.startOffset + appliedDelta)
                                .coerceIn(0f, currentDismissOffset)
                        )
                    }

                            is PlayerDragEvent.End -> {
                                releaseVelocity = event.velocityY
                                break
                            }

                            is PlayerDragEvent.Target -> {
                                targetOverride = event.value
                                break
                            }
                        }
                    }
                }

                val target = targetOverride ?: sheetTarget(
                    offset = state.requireOffset(),
                    velocityY = releaseVelocity,
                    collapsedTopY = currentCollapsedTopY,
                    dismissOffset = currentDismissOffset,
                    minFlingVelocity = minFlingVelocity
                )
                state.animateToWithDecay(target, releaseVelocity)
            }
        }

        BackHandler(enabled = progress > 0f) {
            dragEvents.trySend(PlayerDragEvent.Target(currentCollapsedTopY))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomPadding)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .height(Constants.Ui.MiniPlayer.HEIGHT)
                .graphicsLayer {
                    val offset = state.requireOffset()
                    val layerProgress =
                        ((collapsedTopY - offset) / collapsedTopY).coerceIn(0f, 1f)
                    alpha = 1f - layerProgress
                    translationY = offset - collapsedTopY
                }
                .pointerInput(state, collapsedTopY, dismissOffset) {
                    awaitEachGesture {
                        if (state.requireOffset() < collapsedTopY * 0.99f) {
                            return@awaitEachGesture
                        }

                        val down = awaitFirstDown(requireUnconsumed = false)
                        val pointerId = down.id
                        val startOffset = state.requireOffset()
                        val velocityTracker = VelocityTracker().also {
                            it.addPosition(down.uptimeMillis, down.position)
                        }

                        var rawOffset = 0f
                        var cancelled = false

                        while (true) {
                            val event = awaitPointerEvent()
                            val change =
                                event.changes.firstOrNull { it.id == pointerId } ?: break
                            if (!change.pressed) break

                            velocityTracker.addPosition(change.uptimeMillis, change.position)
                            rawOffset += change.positionChangeIgnoreConsumed().y

                            if (abs(rawOffset) >= touchSlop) {
                                if (dragOwner == null) {
                                    dragOwner = DragZone.Strip
                                }
                                if (dragOwner != DragZone.Strip) {
                                    cancelled = true
                                    break
                                }
                                dragEvents.trySend(PlayerDragEvent.Move(rawOffset, startOffset))
                                change.consume()
                            }
                        }

                        dragOwner = null
                        if (!cancelled) {
                            dragEvents.trySend(
                                PlayerDragEvent.End(velocityTracker.calculateVelocity().y)
                            )
                        }
                    }
                }
                .clickable(
                    enabled = progress < 0.5f,
                    onClick = { internalExpandRequest++ }
                )
        ) {
            MiniPlayer(
                currentSong = currentSong,
                onPlayPause = onPlayPause,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious,
                isPlaying = isPlaying,
                isLoading = isLoading,
            )
        }

        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = progress
                        translationY = collapsedTopY * (1f - progress)
                    }
                    .pointerInput(state, collapsedTopY, dismissOffset) {
                        awaitEachGesture {
                            if (state.requireOffset() >= collapsedTopY * 0.99f) {
                                return@awaitEachGesture
                            }

                            val down = awaitFirstDown(requireUnconsumed = false)
                            val pointerId = down.id
                            val startOffset = state.requireOffset()
                            val velocityTracker = VelocityTracker().also {
                                it.addPosition(down.uptimeMillis, down.position)
                            }

                            var rawOffset = 0f
                            var cancelled = false

                            while (true) {
                                val event = awaitPointerEvent()
                                val change =
                                    event.changes.firstOrNull { it.id == pointerId } ?: break
                                if (!change.pressed) break

                                velocityTracker.addPosition(change.uptimeMillis, change.position)
                                rawOffset += change.positionChangeIgnoreConsumed().y

                                if (abs(rawOffset) >= touchSlop) {
                                    if (dragOwner == null) {
                                        dragOwner = DragZone.Full
                                    }
                                    if (dragOwner != DragZone.Full) {
                                        cancelled = true
                                        break
                                    }
                                    dragEvents.trySend(PlayerDragEvent.Move(rawOffset, startOffset))
                                    change.consume()
                                }
                            }

                            dragOwner = null
                            if (!cancelled) {
                                dragEvents.trySend(
                                    PlayerDragEvent.End(velocityTracker.calculateVelocity().y)
                                )
                            }
                        }
                    }
            ) {
                val collapse: () -> Unit = {
                    dragEvents.trySend(PlayerDragEvent.Target(currentCollapsedTopY))
                }
                fullPlayer(collapse)
            }
        }
    }
}

private fun sheetTarget(
    offset: Float,
    velocityY: Float,
    collapsedTopY: Float,
    dismissOffset: Float,
    minFlingVelocity: Float,
): Float {
    return when {
        offset < collapsedTopY -> when {
            offset <= collapsedTopY / 2f || velocityY <= -minFlingVelocity -> 0f

            else -> collapsedTopY
        }

        offset > collapsedTopY -> when {
            offset >= (collapsedTopY + dismissOffset) / 2f ||
                velocityY >= minFlingVelocity -> dismissOffset

            else -> collapsedTopY
        }

        else -> collapsedTopY
    }
}