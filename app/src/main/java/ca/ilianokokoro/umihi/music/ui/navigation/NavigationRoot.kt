package ca.ilianokokoro.umihi.music.ui.navigation

import android.app.Application
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.media3.common.Player
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.extensions.addNext
import ca.ilianokokoro.umihi.music.extensions.addToQueue
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.components.SongOptionBottomSheet
import ca.ilianokokoro.umihi.music.ui.components.miniplayer.MiniPlayerWrapper
import ca.ilianokokoro.umihi.music.ui.screens.auth.AuthScreen
import ca.ilianokokoro.umihi.music.ui.screens.player.PlayerScreen
import ca.ilianokokoro.umihi.music.ui.screens.playlist.PlaylistScreen
import ca.ilianokokoro.umihi.music.ui.screens.playlists.PlaylistsScreen
import ca.ilianokokoro.umihi.music.ui.screens.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable
private data object PlaylistsScreenKey : NavKey

@Serializable
private data object SettingsScreenKey : NavKey

@Serializable
private data class PlaylistScreenKey(val playlistInfo: PlaylistInfo) : NavKey

@Serializable
private data object AuthScreenKey : NavKey

@Serializable
private data object PlayerScreenKey : NavKey


@Composable
fun NavigationRoot(player: Player, modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(PlaylistsScreenKey) // Start screen
    var songOptionsOpened by remember { mutableStateOf(false) }
    var currentSongOptions by remember { mutableStateOf<Song?>(null) }

    val app = LocalContext.current.applicationContext as Application

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.BottomCenter

    ) {
        NavDisplay(
            modifier = modifier,
            backStack = backStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            transitionSpec = {
                (scaleIn(
                    animationSpec = tween(Constants.Animation.NAVIGATION_DURATION),
                    initialScale = 0.85f
                ) +
                        fadeIn(animationSpec = tween(Constants.Animation.NAVIGATION_DURATION))) togetherWith
                        (scaleOut(
                            animationSpec = tween(Constants.Animation.NAVIGATION_DURATION),
                            targetScale = 1.1f
                        ) +
                                fadeOut(animationSpec = tween(Constants.Animation.NAVIGATION_DURATION)))
            },
            popTransitionSpec = {
                (scaleIn(
                    animationSpec = tween(Constants.Animation.NAVIGATION_DURATION),
                    initialScale = 1.1f
                ) +
                        fadeIn(animationSpec = tween(Constants.Animation.NAVIGATION_DURATION))) togetherWith
                        (scaleOut(
                            animationSpec = tween(Constants.Animation.NAVIGATION_DURATION),
                            targetScale = 0.85f
                        ) +
                                fadeOut(animationSpec = tween(Constants.Animation.NAVIGATION_DURATION)))
            },
            predictivePopTransitionSpec = {
                (scaleIn(
                    animationSpec = tween(Constants.Animation.NAVIGATION_DURATION),
                    initialScale = 1.1f
                ) +
                        fadeIn(animationSpec = tween(Constants.Animation.NAVIGATION_DURATION))) togetherWith
                        (scaleOut(
                            animationSpec = tween(Constants.Animation.NAVIGATION_DURATION),
                            targetScale = 0.85f
                        ) +
                                fadeOut(animationSpec = tween(Constants.Animation.NAVIGATION_DURATION)))
            },
            entryProvider = { key ->

                when (key) {
                    is PlaylistsScreenKey -> {
                        NavEntry(key = key) {
                            PlaylistsScreen(onSettingsButtonPress = {
                                backStack.add(SettingsScreenKey)
                            }, onPlaylistPressed = { playlist ->
                                backStack.add(PlaylistScreenKey(playlistInfo = playlist))
                            }, application = app)
                        }
                    }

                    is SettingsScreenKey -> {
                        NavEntry(key = key) {
                            SettingsScreen(
                                onBack = backStack::removeLastOrNull,
                                openAuthScreen = {
                                    backStack.add(AuthScreenKey)
                                }, application = app
                            )
                        }
                    }

                    is PlaylistScreenKey -> {
                        NavEntry(key = key) {
                            PlaylistScreen(
                                playlistInfo = key.playlistInfo,
                                onBack = backStack::removeLastOrNull,
                                onOpenPlayer = {
                                    backStack.add(PlayerScreenKey)
                                }, onOpenSongOptions = {
                                    currentSongOptions = it
                                    songOptionsOpened = true
                                },

                                player = player, application = app
                            )
                        }
                    }

                    is AuthScreenKey -> {
                        NavEntry(key = key) {
                            AuthScreen(
                                onBack = backStack::removeLastOrNull,
                                application = app
                            )
                        }
                    }

                    is PlayerScreenKey -> {
                        NavEntry(key = key, metadata = Constants.Animation.SLIDE_UP_TRANSITION) {
                            PlayerScreen(
                                onBack = backStack::removeLastOrNull,
                                player = player,
                                application = app
                            )
                        }
                    }

                    else -> throw RuntimeException("Invalid NavKey : $key")
                }
            }
        )

        MiniPlayerWrapper(
            player = player,
            isPlayerOpened = backStack.last() == PlayerScreenKey,
            onMiniPlayerPressed = { backStack.add(PlayerScreenKey) },
            modifier = Modifier
                .systemBarsPadding()
                .padding(2.dp)
        )

        if (songOptionsOpened) {
            SongOptionBottomSheet(
                onAddNext = {
                    if (currentSongOptions != null) {
                        player.addNext(currentSongOptions as Song)
                        songOptionsOpened = false
                    }
                },
                onAddToQueue = {
                    if (currentSongOptions != null) {
                        player.addToQueue(currentSongOptions as Song)
                        songOptionsOpened = false
                    }
                },
                changeVisibility = {
                    songOptionsOpened = false
                })
        }
    }
}