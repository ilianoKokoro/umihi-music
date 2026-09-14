package ca.ilianokokoro.umihi.music.ui.screens.player

import android.app.Application
import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.components.SquareImage
import ca.ilianokokoro.umihi.music.ui.components.bottomsheet.QueueBottomSheet
import ca.ilianokokoro.umihi.music.ui.components.bottomsheet.SleepTimerBottomSheet
import ca.ilianokokoro.umihi.music.ui.components.bottomsheet.SpeedSelectorBottomSheet
import ca.ilianokokoro.umihi.music.ui.components.bottomsheet.VolumeBottomSheet
import ca.ilianokokoro.umihi.music.ui.components.song.ExplicitBadge
import ca.ilianokokoro.umihi.music.ui.screens.player.components.PlayerControls

@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    application: Application,
    playerViewModel: PlayerViewModel = viewModel(
        factory =
            PlayerViewModel.Factory(application = application)
    )
) {
    val uiState = playerViewModel.uiState.collectAsStateWithLifecycle().value
    val orientation = LocalConfiguration.current.orientation
    val currentSong = uiState.queue.getOrNull(uiState.currentIndex)

    // Close the screen if resumed with an empty queue
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && uiState.queue.isEmpty() && currentSong == null) {
                onBack()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        modifier = Modifier.padding(
            start = 8.dp,
            end = 8.dp,
            bottom = 10.dp
        )
    ) { paddingValues ->
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Column(
                modifier = modifier
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .fillMaxSize()
                    .padding(paddingValues),

                horizontalAlignment = Alignment.CenterHorizontally

            ) {

                Thumbnail(
                    href = currentSong?.thumbnailHref.toString(),
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    onSeekBackward = { playerViewModel.seekBy(-10_000L) },
                    onSeekForward = { playerViewModel.seekBy(10_000L) }
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SongInfo(
                        song = currentSong,
                        isLoggedIn = uiState.isLoggedIn,
                        isLiked = uiState.isLiked,
                        isLiking = uiState.isLiking,
                        onToggleLike = playerViewModel::toggleLike,
                    )
                    PlayerControls(
                        isPlaying = uiState.isPlaying,
                        isLoading = uiState.isLoading,
                        progress = playerViewModel.playbackProgress,
                        onSeek = playerViewModel::seek,
                        onSeekPlayer = playerViewModel::seekPlayer,
                        onUpdateSeekBarHeldState = playerViewModel::updateSeekBarHeldState,
                        onOpenQueue = {
                            playerViewModel.setQueueVisibility(true)
                        },
                        onOpenVolume = {
                            playerViewModel.updateShowVolumeDialog(true)
                        },
                        onOpenSleepTimer = {
                            playerViewModel.setSleepTimerSheetVisibility(true)
                        },
                        onOpenSpeedSelector = {
                            playerViewModel.setSpeedSelectorVisibility(true)
                        },
                        playbackSpeed = uiState.playbackSpeed,
                        sleepTimerRemainingSeconds = uiState.sleepTimerRemainingSeconds,
                    )
                }
            }
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Row(
                modifier = modifier
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                ) {
                    Thumbnail(
                        href = currentSong?.thumbnailHref.toString(),
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        onSeekBackward = { playerViewModel.seekBy(-10_000L) },
                        onSeekForward = { playerViewModel.seekBy(10_000L) }
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .padding(horizontal = 32.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    SongInfo(
                        song = currentSong,
                        isLoggedIn = uiState.isLoggedIn,
                        isLiked = uiState.isLiked,
                        isLiking = uiState.isLiking,
                        onToggleLike = playerViewModel::toggleLike,
                    )

                    PlayerControls(
                        isPlaying = uiState.isPlaying,
                        isLoading = uiState.isLoading,
                        progress = playerViewModel.playbackProgress,
                        onSeek = playerViewModel::seek,
                        onSeekPlayer = playerViewModel::seekPlayer,
                        onUpdateSeekBarHeldState = playerViewModel::updateSeekBarHeldState,
                        onOpenQueue = {
                            playerViewModel.setQueueVisibility(true)
                        },
                        onOpenVolume = {
                            playerViewModel.updateShowVolumeDialog(true)
                        },
                        onOpenSleepTimer = {
                            playerViewModel.setSleepTimerSheetVisibility(true)
                        },
                        onOpenSpeedSelector = {
                            playerViewModel.setSpeedSelectorVisibility(true)
                        },
                        playbackSpeed = uiState.playbackSpeed,
                        sleepTimerRemainingSeconds = uiState.sleepTimerRemainingSeconds,
                    )
                }
            }

        }
    }


    if (uiState.isSpeedSelectorShown) {
        SpeedSelectorBottomSheet(
            changeVisibility = playerViewModel::setSpeedSelectorVisibility,
            currentSpeed = uiState.playbackSpeed,
            onSelectSpeed = playerViewModel::setPlaybackSpeed,
        )
    } else if (uiState.isQueueModalShown) {
        QueueBottomSheet(
            changeVisibility = playerViewModel::setQueueVisibility,
            songs = uiState.queue,
            currentIndex = uiState.currentIndex
        )
    } else if (uiState.isSleepTimerModalShown) {
        SleepTimerBottomSheet(
            changeVisibility = playerViewModel::setSleepTimerSheetVisibility,
            activeRemainingSeconds = uiState.sleepTimerRemainingSeconds,
            onStartTimer = playerViewModel::startSleepTimer,
            onStartEndOfSong = playerViewModel::startSleepTimerEndOfSong,
            onCancelTimer = playerViewModel::cancelSleepTimer,
        )
    } else if (uiState.showVolumeDialog) {
        VolumeBottomSheet(
            changeVisibility = playerViewModel::updateShowVolumeDialog,
            currentVolume = uiState.appVolume,
            onVolumeChange = playerViewModel::setAppVolume
        )
    }
}

private enum class SeekDirection { BACKWARD, FORWARD }

private data class SeekFeedback(
    val direction: SeekDirection,
    val seconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun Thumbnail(
    href: String,
    modifier: Modifier = Modifier,
    onSeekBackward: () -> Unit = {},
    onSeekForward: () -> Unit = {},
) {
    var seekFeedback by remember { mutableStateOf<SeekFeedback?>(null) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(seekFeedback?.timestamp) {
        if (seekFeedback != null) {
            delay(650)
            seekFeedback = null
        }
    }

    BoxWithConstraints(
        modifier = modifier.padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        val size = minOf(maxWidth, maxHeight)

        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { offset ->
                            val isLeft = offset.x < this.size.width / 2f
                            val direction = if (isLeft) SeekDirection.BACKWARD else SeekDirection.FORWARD
                            val now = System.currentTimeMillis()
                            val current = seekFeedback
                            val newSeconds = if (current != null && current.direction == direction && (now - current.timestamp) < 700) {
                                current.seconds + 10
                            } else {
                                10
                            }
                            seekFeedback = SeekFeedback(direction, newSeconds, now)
                            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                            if (isLeft) {
                                onSeekBackward()
                            } else {
                                onSeekForward()
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = href,
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(Constants.Player.IMAGE_TRANSITION_DELAY)
                    ).togetherWith(
                        fadeOut(
                            animationSpec = tween(Constants.Player.IMAGE_TRANSITION_DELAY)
                        )
                    )
                }
            ) { targetState ->
                SquareImage(
                    uri = targetState,
                    modifier = Modifier.size(size)
                )
            }

            // Seek backward overlay (left half)
            AnimatedVisibility(
                visible = seekFeedback?.direction == SeekDirection.BACKWARD,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.8f),
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f)
                    .align(Alignment.CenterStart)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.6f),
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FastRewind,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "-${seekFeedback?.seconds ?: 10}s",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Seek forward overlay (right half)
            AnimatedVisibility(
                visible = seekFeedback?.direction == SeekDirection.FORWARD,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.8f),
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f)
                    .align(Alignment.CenterEnd)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Black.copy(alpha = 0.6f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FastForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "+${seekFeedback?.seconds ?: 10}s",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SongInfo(
    song: Song?,
    isLoggedIn: Boolean = false,
    isLiked: Boolean = false,
    isLiking: Boolean = false,
    onToggleLike: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = if (isLoggedIn) {
                Modifier.weight(1f)
            } else {
                Modifier.fillMaxWidth()
            },
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (song?.isExplicit == true) {
                    ExplicitBadge()
                }
                Text(
                    text = song?.title ?: "",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.basicMarquee()
                )
            }
            Text(
                text = song?.artist ?: "",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.basicMarquee()
            )
        }

        if (isLoggedIn) {
            Box(modifier = Modifier.padding(start = 8.dp)) {
                FilledIconToggleButton(
                    checked = isLiked,
                    onCheckedChange = {
                        if (isLiking) {
                            return@FilledIconToggleButton
                        }
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        onToggleLike()
                    },
                    shapes = IconButtonDefaults.toggleableShapes(),
                    colors = IconButtonDefaults.filledIconToggleButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        checkedContainerColor = IconButtonDefaults.filledIconToggleButtonColors().checkedContainerColor,
                        checkedContentColor = IconButtonDefaults.filledIconToggleButtonColors().checkedContentColor,
                    ),
                ) {
                    Icon(
                        imageVector = if (isLiked) {
                            Icons.Rounded.Favorite
                        } else {
                            Icons.Rounded.FavoriteBorder
                        },
                        contentDescription = if (isLiked) {
                            stringResource(R.string.unlike)
                        } else {
                            stringResource(R.string.like)
                        }
                    )
                }
            }
        }
    }
}