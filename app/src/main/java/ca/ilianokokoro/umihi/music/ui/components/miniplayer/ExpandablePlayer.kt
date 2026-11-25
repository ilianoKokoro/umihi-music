package ca.ilianokokoro.umihi.music.ui.components.miniplayer

import android.app.Application
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.managers.PlayerManager
import ca.ilianokokoro.umihi.music.extensions.placedWhen
import ca.ilianokokoro.umihi.music.ui.screens.player.PlayerScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.lerp as lerpColor

private enum class PlayerAnchor { Expanded, Collapsed, Dismissed }

private object ExpandablePlayerSpec {
    const val DISMISS_DRAG_TRAVEL_MULTIPLIER = 1.5f
    const val EXPAND_POSITION_THRESHOLD = 0.5f
    const val DISMISS_POSITION_THRESHOLD = 0.4f
    const val MINI_PLAYER_VISIBLE_UNTIL_PROGRESS = 0.25f
    const val FULL_PLAYER_VISIBLE_FROM_PROGRESS = 0.2f

    val FLING_VELOCITY_THRESHOLD = 125.dp
    val COLLAPSED_CORNER_RADIUS = 16.dp
    val EXPANDED_CORNER_RADIUS = 0.dp
    val COLLAPSED_OUTER_PADDING = 4.dp
    val CARD_ELEVATION = 4.dp
}

private fun fullPlayerAlpha(expansionProgress: Float): Float {
    val fadeStart = ExpandablePlayerSpec.FULL_PLAYER_VISIBLE_FROM_PROGRESS
    val fadeRange = 1f - fadeStart
    return ((expansionProgress - fadeStart) / fadeRange).coerceIn(0f, 1f)
}

private fun miniPlayerAlpha(expansionProgress: Float): Float {
    return (1f - expansionProgress / ExpandablePlayerSpec.MINI_PLAYER_VISIBLE_UNTIL_PROGRESS)
        .coerceIn(0f, 1f)
}

@Composable
fun ExpandablePlayer(
    modifier: Modifier = Modifier,
    bottomPadding: Dp,
    application: Application,
    onClose: () -> Unit,
    bottomBar: @Composable () -> Unit = {},
) {

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        val fullHeight = this.maxHeight

        val currentSongId by PlayerManager.currentSongId.collectAsStateWithLifecycle()
        val isVisible = currentSongId != null

        val miniPx = with(density) { Constants.Ui.MiniPlayer.HEIGHT.toPx() }
        val bottomPx = with(density) { bottomPadding.toPx() }
        val outerPadPx = with(density) { ExpandablePlayerSpec.COLLAPSED_OUTER_PADDING.toPx() }

        val collapsedPx = constraints.maxHeight - miniPx - bottomPx - outerPadPx
        val dismissedPx =
            collapsedPx + (miniPx + bottomPx + outerPadPx) * ExpandablePlayerSpec.DISMISS_DRAG_TRAVEL_MULTIPLIER
        val flingPx = with(density) { ExpandablePlayerSpec.FLING_VELOCITY_THRESHOLD.toPx() }

        var savedAnchor by rememberSaveable { mutableStateOf(PlayerAnchor.Collapsed) }

        val state = remember(collapsedPx, dismissedPx) {
            AnchoredDraggableState(
                initialValue = if (savedAnchor == PlayerAnchor.Dismissed) {
                    PlayerAnchor.Collapsed
                } else {
                    savedAnchor
                },
                anchors = DraggableAnchors {
                    PlayerAnchor.Expanded at 0f
                    PlayerAnchor.Collapsed at collapsedPx
                    PlayerAnchor.Dismissed at dismissedPx
                },
            )
        }

        val offset by remember(state, collapsedPx) {
            derivedStateOf { state.requireOffset() }
        }

        val expansionProgress by remember(state, collapsedPx) {
            derivedStateOf {
                (1f - offset / collapsedPx).coerceIn(0f, 1f)
            }
        }
        val expandSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
        val collapseSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()

        fun settle(target: PlayerAnchor, spec: AnimationSpec<Float> = expandSpec) {
            scope.launch { state.animateTo(target, spec) }
        }

        val currentState by rememberUpdatedState(state)

        LaunchedEffect(Unit) {
            PlayerManager.playerOpenRequests.collect {
                currentState.animateTo(PlayerAnchor.Expanded, expandSpec)
            }
        }

        LaunchedEffect(isVisible) {
            if (!isVisible) {
                state.snapTo(PlayerAnchor.Collapsed)
            }
        }

        val miniActive by remember(expansionProgress) {
            derivedStateOf {
                expansionProgress <= ExpandablePlayerSpec.MINI_PLAYER_VISIBLE_UNTIL_PROGRESS
            }
        }

        val fullActive by remember(expansionProgress) {
            derivedStateOf {
                expansionProgress >= ExpandablePlayerSpec.FULL_PLAYER_VISIBLE_FROM_PROGRESS
            }
        }
        // Holds the anchoredDrag mutex while the finger is down so a running settle animation is interrupted.
        val grab = remember { mutableStateOf<Job?>(null) }

        val dragState = rememberDraggableState { delta -> state.dispatchRawDelta(delta) }

        LaunchedEffect(state.settledValue) {
            if (state.settledValue == PlayerAnchor.Dismissed) {
                onClose()
            } else {
                savedAnchor = state.settledValue
            }
        }

        PredictiveBackHandler(
            enabled = state.currentValue == PlayerAnchor.Expanded ||
                    state.targetValue == PlayerAnchor.Expanded
        ) { events ->
            try {
                state.anchoredDrag {
                    events.collect { event ->
                        dragTo(collapsedPx * event.progress)
                    }
                }
            } catch (e: CancellationException) {
                state.animateTo(PlayerAnchor.Expanded, expandSpec)
                throw e
            }

            state.animateTo(PlayerAnchor.Collapsed, collapseSpec)
        }

        if (isVisible) {
            val progress = expansionProgress
            val offsetPx = offset.coerceAtLeast(0f)

            val cardHeightPx = if (offsetPx >= collapsedPx) {
                miniPx
            } else {
                (constraints.maxHeight - (bottomPx + outerPadPx) * (1f - progress) - offsetPx)
                    .coerceAtLeast(miniPx)
            }
            val cardHeight = with(density) { cardHeightPx.toDp() }

            val sidePadding = lerp(
                ExpandablePlayerSpec.COLLAPSED_OUTER_PADDING,
                0.dp,
                progress
            )
            val cornerRadius = lerp(
                ExpandablePlayerSpec.COLLAPSED_CORNER_RADIUS,
                ExpandablePlayerSpec.EXPANDED_CORNER_RADIUS,
                progress
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, offsetPx.roundToInt()) }
                    .padding(horizontal = sidePadding)
                    .height(cardHeight)
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
                                    if (offset - collapsedPx >= (dismissedPx - collapsedPx) * ExpandablePlayerSpec.DISMISS_POSITION_THRESHOLD)
                                        PlayerAnchor.Dismissed else PlayerAnchor.Collapsed

                                else ->
                                    if (offset <= collapsedPx * ExpandablePlayerSpec.EXPAND_POSITION_THRESHOLD)
                                        PlayerAnchor.Expanded else PlayerAnchor.Collapsed
                            }
                            settle(target)
                        }
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = state.settledValue == PlayerAnchor.Collapsed
                    ) {
                        settle(PlayerAnchor.Expanded)
                    },
                shape = RoundedCornerShape(cornerRadius),
                colors = CardDefaults.cardColors(
                    containerColor = lerpColor(
                        MaterialTheme.colorScheme.surfaceContainerHighest,
                        MaterialTheme.colorScheme.surfaceContainerLow,
                        progress
                    )
                ),
                elevation = CardDefaults.cardElevation(ExpandablePlayerSpec.CARD_ELEVATION)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(Alignment.Top, unbounded = true)
                        .height(fullHeight)
                ) {
                    Box(
                        Modifier
                            .placedWhen(fullActive)
                            .fillMaxSize()
                            .graphicsLayer { alpha = fullPlayerAlpha(progress) }
                    ) {
                        PlayerScreen(application = application)
                    }

                    Box(
                        Modifier
                            .placedWhen(miniActive)
                            .fillMaxWidth()
                            .height(Constants.Ui.MiniPlayer.HEIGHT)
                            .graphicsLayer { alpha = miniPlayerAlpha(progress) }
                    ) {
                        MiniPlayer()
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    translationY = expansionProgress * size.height
                },
        ) {
            bottomBar()
        }
    }
}