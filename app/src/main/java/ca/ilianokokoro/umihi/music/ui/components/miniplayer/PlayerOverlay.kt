package ca.ilianokokoro.umihi.music.ui.components.miniplayer

import android.app.Application
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.ui.screens.player.PlayerScreen
import kotlin.math.roundToInt

enum class PlayerSheetState {
    Closed,
    MiniPlayer,
    FullPlayer
}

@Composable
fun PlayerOverlay(
    visible: Boolean,
    onClose: () -> Unit,
    application: Application,
    modifier: Modifier = Modifier,
) {
    var mode by remember { mutableStateOf(PlayerSheetState.Closed) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(visible) {
        mode = if (visible) PlayerSheetState.MiniPlayer else PlayerSheetState.Closed
        dragOffsetY = 0f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 8.dp)
    ) {
        when (mode) {
            PlayerSheetState.Closed -> Unit

            PlayerSheetState.MiniPlayer -> {
                MiniPlayerWrapper(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset {
                            IntOffset(
                                x = 0,
                                y = dragOffsetY.roundToInt()
                            )
                        }
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { delta ->
                                dragOffsetY = (dragOffsetY + delta)
                                    .coerceIn(-300f, 300f)
                            },
                            onDragStopped = { velocity ->
                                when {
                                    dragOffsetY < -120f || velocity < -500f -> {
                                        dragOffsetY = 0f
                                        mode = PlayerSheetState.FullPlayer
                                    }

                                    dragOffsetY > 80f || velocity > 500f -> {
                                        dragOffsetY = 0f
                                        mode = PlayerSheetState.Closed
                                        onClose()
                                    }

                                    else -> {
                                        dragOffsetY = 0f
                                    }
                                }
                            }
                        )
                )
            }

            PlayerSheetState.FullPlayer -> {
                PlayerScreen(
                    modifier = modifier
                        .fillMaxSize()
                        .offset {
                            IntOffset(
                                x = 0,
                                y = dragOffsetY.roundToInt()
                            )
                        }
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { delta ->
                                dragOffsetY = (dragOffsetY + delta).coerceAtLeast(0f)
                            },
                            onDragStopped = { velocity ->
                                if (dragOffsetY > 120f || velocity > 500f) {
                                    dragOffsetY = 0f
                                    mode = PlayerSheetState.MiniPlayer
                                } else {
                                    dragOffsetY = 0f
                                }
                            }
                        ),
                    onBack = {
                        mode = PlayerSheetState.MiniPlayer
                    },
                    application = application
                )
            }
        }
    }
}