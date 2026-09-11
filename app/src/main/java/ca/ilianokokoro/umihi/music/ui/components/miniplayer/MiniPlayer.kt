package ca.ilianokokoro.umihi.music.ui.components.miniplayer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.helpers.ComposeHelper
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.components.SquareImage
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class MiniPlayerDragDirection {
    HORIZONTAL,
    VERTICAL
}

@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    currentSong: Song,
    onClick: () -> Unit,
    onDismiss: () -> Unit = {},
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    isPlaying: Boolean,
    isLoading: Boolean,
) {
    val controlsInteractionSources = List(3) { ComposeHelper.rememberInteractionSource() }
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current

    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    val horizontalThresholdPx = with(density) { 48.dp.toPx() }
    val verticalThresholdPx = with(density) { 36.dp.toPx() }
    val velocityThresholdPx = with(density) { 500.dp.toPx() }
    val maxHorizontalDragPx = with(density) { 150.dp.toPx() }
    val maxVerticalUpDragPx = with(density) { 50.dp.toPx() }
    val maxVerticalDownDragPx = with(density) { 90.dp.toPx() }

    val gestureModifier = Modifier.pointerInput(Unit) {
        val touchSlop = viewConfiguration.touchSlop
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val velocityTracker = VelocityTracker()
            velocityTracker.addPosition(down.uptimeMillis, down.position)

            var totalDragX = 0f
            var totalDragY = 0f
            var isDragging = false
            var dragDirection: MiniPlayerDragDirection? = null
            var gestureCompletedNormally = false

            try {
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) {
                        break
                    }

                    velocityTracker.addPosition(change.uptimeMillis, change.position)
                    val dragAmount = change.positionChange()
                    totalDragX += dragAmount.x
                    totalDragY += dragAmount.y

                    if (!isDragging) {
                        val absX = abs(totalDragX)
                        val absY = abs(totalDragY)
                        if (absX > touchSlop || absY > touchSlop) {
                            isDragging = true
                            dragDirection =
                                if (absX >= absY) MiniPlayerDragDirection.HORIZONTAL
                                else MiniPlayerDragDirection.VERTICAL
                            change.consume()
                        }
                    } else {
                        change.consume()
                        if (dragDirection == MiniPlayerDragDirection.HORIZONTAL) {
                            coroutineScope.launch {
                                val newX = (offsetX.value + dragAmount.x)
                                    .coerceIn(-maxHorizontalDragPx, maxHorizontalDragPx)
                                offsetX.snapTo(newX)
                            }
                        } else if (dragDirection == MiniPlayerDragDirection.VERTICAL) {
                            coroutineScope.launch {
                                val damping = if (dragAmount.y < 0) 0.5f else 0.8f
                                val newY = (offsetY.value + dragAmount.y * damping)
                                    .coerceIn(-maxVerticalUpDragPx, maxVerticalDownDragPx)
                                offsetY.snapTo(newY)
                            }
                        }
                    }
                }

                if (isDragging) {
                    val velocity = velocityTracker.calculateVelocity()
                    if (dragDirection == MiniPlayerDragDirection.HORIZONTAL) {
                        val currentX = offsetX.value
                        val vx = velocity.x
                        if (currentX < -horizontalThresholdPx || vx < -velocityThresholdPx) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                            onSkipNext()
                        } else if (currentX > horizontalThresholdPx || vx > velocityThresholdPx) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                            onSkipPrevious()
                        }
                        coroutineScope.launch {
                            offsetX.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    } else if (dragDirection == MiniPlayerDragDirection.VERTICAL) {
                        val currentY = offsetY.value
                        val vy = velocity.y
                        if (currentY < -verticalThresholdPx || vy < -velocityThresholdPx) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                            onClick()
                        } else if (currentY > verticalThresholdPx || vy > velocityThresholdPx) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                            onDismiss()
                        }
                        coroutineScope.launch {
                            offsetY.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    }
                }
                gestureCompletedNormally = true
            } finally {
                if (!gestureCompletedNormally) {
                    if (offsetX.value != 0f) {
                        coroutineScope.launch {
                            offsetX.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    }
                    if (offsetY.value != 0f) {
                        coroutineScope.launch {
                            offsetY.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .then(gestureModifier)
            .clickable(onClick = onClick),
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
                    text = currentSong.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.basicMarquee()
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
                            modifier = Modifier.animateWidth(
                                interactionSource = controlsInteractionSources[0]
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipPrevious,
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
                            shapes = IconButtonDefaults.toggleableShapes()
                                .copy(checkedShape = IconButtonDefaults.shapes().shape),
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
                                    Icons.Rounded.Pause
                                } else {
                                    Icons.Rounded.PlayArrow
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
                            shapes = IconButtonDefaults.shapes(),
                            interactionSource = controlsInteractionSources[2],
                            modifier = Modifier.animateWidth(
                                interactionSource = controlsInteractionSources[2]
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
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
