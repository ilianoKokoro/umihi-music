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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import ca.ilianokokoro.umihi.music.core.helpers.ComposeHelper
import ca.ilianokokoro.umihi.music.core.managers.PlayerManager
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.components.SquareImage
import ca.ilianokokoro.umihi.music.ui.components.bottomsheet.QueueBottomSheet
import ca.ilianokokoro.umihi.music.ui.components.bottomsheet.SleepTimerBottomSheet
import ca.ilianokokoro.umihi.music.ui.components.bottomsheet.SpeedSelectorBottomSheet
import ca.ilianokokoro.umihi.music.ui.components.bottomsheet.VolumeBottomSheet
import ca.ilianokokoro.umihi.music.ui.components.song.ExplicitBadge
import ca.ilianokokoro.umihi.music.ui.screens.player.components.PlayerControls
import ca.ilianokokoro.umihi.music.ui.screens.player.components.TopPlayer

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
    val context = LocalContext.current
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
    val playbackProgress by playerViewModel.playbackProgress.collectAsState()


    Scaffold(
        modifier = Modifier
            .padding(
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

                TopPlayer(
                    currentSong = currentSong,
                    isLyricsShown = uiState.lyricsShown,
                    lyricsState = uiState.lyrics,
                    positionMs = { playbackProgress.position.toLong() },
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
                        onShare = { currentSong?.let { playerViewModel.shareSong(context, it) } },
                    )

                    PlayerControlsSection(
                        uiState = uiState,
                        playerViewModel = playerViewModel,
                        onStartRadio = {
                            currentSong?.let { PlayerManager.playRadio(it, application) }
                        }
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
                    TopPlayer(
                        currentSong = currentSong,
                        isLyricsShown = uiState.lyricsShown,
                        lyricsState = uiState.lyrics,
                        positionMs = { playbackProgress.position.toLong() },
                        modifier = Modifier
                            .fillMaxHeight()
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
                        onShare = { currentSong?.let { playerViewModel.shareSong(context, it) } },
                    )

                    PlayerControlsSection(
                        uiState = uiState,
                        playerViewModel = playerViewModel,
                        onStartRadio = {
                            currentSong?.let { PlayerManager.playRadio(it, application) }
                        }
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
            currentIndex = uiState.currentIndex,
            onStartRadio = { song -> PlayerManager.playRadio(song, application) }
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

private fun Modifier.swipeUpToOpen(
    thresholdPx: Float,
    onTriggered: () -> Unit,
): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        var accumulatedDrag = 0f
        var triggered = false
        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent(PointerEventPass.Main)
            val change = event.changes.firstOrNull { it.pressed } ?: break
            if (triggered) {
                change.consume()
            } else {
                val dy = change.position.y - change.previousPosition.y
                accumulatedDrag += dy
                if (accumulatedDrag < -thresholdPx) {
                    triggered = true
                    onTriggered()
                    change.consume()
                }
            }
        } while (event.changes.any { it.pressed })
    }
}

@Composable
private fun PlayerControlsSection(
    uiState: PlayerState,
    playerViewModel: PlayerViewModel,
    onStartRadio: () -> Unit,
) {
    val density = LocalDensity.current
    val swipeUpThresholdPx = with(density) { 48.dp.toPx() }

    PlayerControls(
        isPlaying = uiState.isPlaying,
        isLoading = uiState.isLoading,
        isLyricsShown = uiState.lyricsShown,
        progress = playerViewModel.playbackProgress,
        onSeek = playerViewModel::seek,
        onSeekPlayer = playerViewModel::seekPlayer,
        onUpdateSeekBarHeldState = playerViewModel::updateSeekBarHeldState,
        onOpenQueue = { playerViewModel.setQueueVisibility(true) },
        onOpenVolume = { playerViewModel.updateShowVolumeDialog(true) },
        onOpenSleepTimer = { playerViewModel.setSleepTimerSheetVisibility(true) },
        onOpenSpeedSelector = { playerViewModel.setSpeedSelectorVisibility(true) },
        playbackSpeed = uiState.playbackSpeed,
        onToggleLyrics = playerViewModel::toggleLyrics,
        onStartRadio = onStartRadio,
        sleepTimerRemainingSeconds = uiState.sleepTimerRemainingSeconds,
        modifier = Modifier.swipeUpToOpen(swipeUpThresholdPx) {
            playerViewModel.setQueueVisibility(true)
        },
    )
}


@Composable
fun SongInfo(
    song: Song?,
    isLoggedIn: Boolean = false,
    isLiked: Boolean = false,
    isLiking: Boolean = false,
    onToggleLike: () -> Unit = {},
    onShare: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    val actionButtonInteractionSources =
        List(2) { ComposeHelper.rememberInteractionSource() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
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
                text = song?.artists ?: "",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.basicMarquee()
            )
        }

        Box(modifier = Modifier.padding(start = 8.dp)) {
            if (isLoggedIn) {
                ButtonGroup(
                    overflowIndicator = {},
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                ) {
                    customItem(
                        buttonGroupContent = {
                            FilledIconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                                    onShare()
                                },
                                interactionSource = actionButtonInteractionSources[0],
                                shapes = IconButtonDefaults.shapes(
                                    shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                                    pressedShape = ButtonGroupDefaults.connectedLeadingButtonPressShape,
                                ),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier
                                    .animateWidth(interactionSource = actionButtonInteractionSources[0])

                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = stringResource(R.string.share)
                                )
                            }

                        },
                        menuContent = {}
                    )
                    customItem(
                        buttonGroupContent = {
                            FilledIconToggleButton(
                                checked = isLiked,
                                onCheckedChange = {
                                    if (isLiking) {
                                        return@FilledIconToggleButton
                                    }
                                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                                    onToggleLike()
                                },
                                interactionSource = actionButtonInteractionSources[1],
                                shapes = IconButtonDefaults.toggleableShapes(
                                    shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                                    pressedShape = ButtonGroupDefaults.connectedTrailingButtonPressShape,
                                    checkedShape = ButtonGroupDefaults.connectedTrailingButtonShape,
                                ),
                                colors = IconButtonDefaults.filledIconToggleButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    checkedContainerColor = IconButtonDefaults.filledIconToggleButtonColors().checkedContainerColor,
                                    checkedContentColor = IconButtonDefaults.filledIconToggleButtonColors().checkedContentColor,
                                ),
                                modifier = Modifier
                                    .animateWidth(interactionSource = actionButtonInteractionSources[1])
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
                        },
                        menuContent = {}
                    )
                }
            } else {
                FilledIconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        onShare()
                    },
                    shapes = IconButtonDefaults.shapes(),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = stringResource(R.string.share)
                    )
                }
            }
        }
    }
}
