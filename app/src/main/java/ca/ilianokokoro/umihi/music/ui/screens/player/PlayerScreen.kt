package ca.ilianokokoro.umihi.music.ui.screens.player

import android.app.Application
import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.managers.PlayerManager
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.components.SquareImage
import ca.ilianokokoro.umihi.music.ui.screens.player.components.PlayerControls
import ca.ilianokokoro.umihi.music.ui.screens.player.components.QueueBottomSheet
import ca.ilianokokoro.umihi.music.ui.screens.player.components.SleepTimerBottomSheet
import ca.ilianokokoro.umihi.music.ui.screens.player.components.SpeedSelectorBottomSheet

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
    val playbackData = PlayerManager.audioInfo.collectAsStateWithLifecycle().value
    val orientation = LocalConfiguration.current.orientation
    val currentSong = uiState.queue.getOrNull(uiState.currentIndex)

    // Close the screen in resumed with an empty queue
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

    Scaffold { paddingValues ->
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Column(
                modifier = modifier
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .fillMaxSize()
                    .padding(end = 8.dp, start = 8.dp, top = paddingValues.calculateTopPadding()),

                horizontalAlignment = Alignment.CenterHorizontally

            ) {

                Thumbnail(
                    href = currentSong?.thumbnailHref.toString(),
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
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
                        progress = uiState.playbackProgress,
                        onSeek = playerViewModel::seek,
                        onSeekPlayer = playerViewModel::seekPlayer,
                        onUpdateSeekBarHeldState = playerViewModel::updateSeekBarHeldState,
                        onOpenQueue = {
                            playerViewModel.setQueueVisibility(true)
                        },
                        onOpenSleepTimer = {
                            playerViewModel.setSleepTimerSheetVisibility(true)
                        },
                        onOpenSpeedSelector = {
                            playerViewModel.setSpeedSelectorVisibility(true)
                        },
                        playbackSpeed = uiState.playbackSpeed,
                        sleepTimerRemainingSeconds = uiState.sleepTimerRemainingSeconds,
                        audioInfo = playbackData,
                    )
                }
            }
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Row(
                modifier = modifier
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(
                        end = 8.dp,
                        start = 8.dp,
                        top = paddingValues.calculateTopPadding()
                    )
                    .fillMaxSize(),
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
                            .weight(1f)
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
                        progress = uiState.playbackProgress,
                        onSeek = playerViewModel::seek,
                        onSeekPlayer = playerViewModel::seekPlayer,
                        onUpdateSeekBarHeldState = playerViewModel::updateSeekBarHeldState,
                        onOpenQueue = {
                            playerViewModel.setQueueVisibility(true)
                        },
                        onOpenSleepTimer = {
                            playerViewModel.setSleepTimerSheetVisibility(true)
                        },
                        onOpenSpeedSelector = {
                            playerViewModel.setSpeedSelectorVisibility(true)
                        },
                        playbackSpeed = uiState.playbackSpeed,
                        sleepTimerRemainingSeconds = uiState.sleepTimerRemainingSeconds,
                        audioInfo = playbackData,
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
    }

    if (uiState.isQueueModalShown) {
        QueueBottomSheet(
            changeVisibility = playerViewModel::setQueueVisibility,
            songs = uiState.queue,
            currentIndex = uiState.currentIndex
        )
    }

    if (uiState.isSleepTimerModalShown) {
        SleepTimerBottomSheet(
            changeVisibility = playerViewModel::setSleepTimerSheetVisibility,
            activeRemainingSeconds = uiState.sleepTimerRemainingSeconds,
            onStartTimer = playerViewModel::startSleepTimer,
            onStartEndOfSong = playerViewModel::startSleepTimerEndOfSong,
            onCancelTimer = playerViewModel::cancelSleepTimer,
        )
    }
}

@Composable
fun Thumbnail(
    href: String,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        val size = minOf(maxWidth, maxHeight)

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
            Text(
                text = song?.title ?: "",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.basicMarquee()
            )
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
            FilledTonalIconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    onToggleLike()
                },
                enabled = !isLiking,
                shapes = IconButtonDefaults.shapes()
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
                    },
                    tint = if (isLiked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}