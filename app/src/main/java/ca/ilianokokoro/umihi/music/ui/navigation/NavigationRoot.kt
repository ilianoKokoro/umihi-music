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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.media3.common.Player
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.ui.screens.auth.AuthScreen
import ca.ilianokokoro.umihi.music.ui.screens.player.PlayerScreen
import ca.ilianokokoro.umihi.music.ui.screens.playlist.PlaylistScreen
import ca.ilianokokoro.umihi.music.ui.screens.playlists.PlaylistsScreen
import ca.ilianokokoro.umihi.music.ui.screens.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable
data object PlaylistsScreenKey : NavKey

@Serializable
data object SettingsScreenKey : NavKey

@Serializable
data class PlaylistScreenKey(val playlist: Playlist) : NavKey

@Serializable
data object AuthScreenKey : NavKey

@Serializable
data object PlayerScreenKey : NavKey


@Composable
fun NavigationRoot(player: Player, modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(PlaylistsScreenKey) // Start screen

    val app = LocalContext.current.applicationContext as Application

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NavDisplay(
            modifier = modifier,
            backStack = backStack,
            entryDecorators = listOf(
                rememberSavedStateNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
                rememberSceneSetupNavEntryDecorator()
            ), transitionSpec = {
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
            }, entryProvider = { key ->

                when (key) {
                    is PlaylistsScreenKey -> {
                        NavEntry(key = key) {
                            PlaylistsScreen(onSettingsButtonPress = {
                                backStack.add(SettingsScreenKey)
                            }, onPlaylistPressed = { playlist ->
                                backStack.add(PlaylistScreenKey(playlist = playlist))
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
                                playlist = key.playlist,
                                onBack = backStack::removeLastOrNull,
                                onOpenPlayer = {
                                    backStack.add(PlayerScreenKey)
                                }, player = player, application = app
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
                        NavEntry(key = key) {
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

    }

}