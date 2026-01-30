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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.media3.common.Player
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printe
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.ui.components.miniplayer.MiniPlayerWrapper
import ca.ilianokokoro.umihi.music.ui.screens.auth.AuthScreen
import ca.ilianokokoro.umihi.music.ui.screens.home.HomeScreen
import ca.ilianokokoro.umihi.music.ui.screens.player.PlayerScreen
import ca.ilianokokoro.umihi.music.ui.screens.playlist.PlaylistScreen
import ca.ilianokokoro.umihi.music.ui.screens.search.SearchScreen
import ca.ilianokokoro.umihi.music.ui.screens.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable
data object HomeScreenKey : NavKey

@Serializable
data object SearchScreenKey : NavKey

@Serializable
data object SettingsScreenKey : NavKey

@Serializable
private data class PlaylistScreenKey(val playlistInfo: PlaylistInfo) : NavKey

@Serializable
private data object AuthScreenKey : NavKey

@Serializable
private data object PlayerScreenKey : NavKey


@Composable
fun NavigationRoot(player: Player, modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(HomeScreenKey)
    val app = LocalContext.current.applicationContext as Application

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                currentTab = backStack.last(),
                onTabSelected = { key ->
                    if (backStack.last() != key) backStack.add(key)
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.BottomCenter
        ) {
            NavDisplay(
                modifier = Modifier.fillMaxSize(),
                backStack = backStack,
                onBack = backStack::safePop,
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
                        is HomeScreenKey -> NavEntry(key) {
                            HomeScreen(
                                onSettingsButtonPress = { backStack.add(SettingsScreenKey) },
                                onPlaylistPressed = { playlist ->
                                    backStack.add(PlaylistScreenKey(playlistInfo = playlist))
                                },
                                application = app
                            )
                        }

                        is SettingsScreenKey -> NavEntry(key) {
                            SettingsScreen(
                                openAuthScreen = { backStack.add(AuthScreenKey) },
                                application = app
                            )
                        }

                        is PlaylistScreenKey -> NavEntry(key) {
                            PlaylistScreen(
                                playlistInfo = key.playlistInfo,
                                onBack = backStack::safePop,
                                onOpenPlayer = { backStack.add(PlayerScreenKey) },
                                player = player,
                                application = app
                            )
                        }

                        is AuthScreenKey -> NavEntry(key) {
                            AuthScreen(
                                onBack = backStack::safePop,
                                application = app
                            )
                        }

                        is PlayerScreenKey -> NavEntry(
                            key,
                            metadata = Constants.Animation.SLIDE_UP_TRANSITION
                        ) {
                            PlayerScreen(
                                onBack = backStack::safePop,
                                player = player,
                                application = app
                            )
                        }

                        is SearchScreenKey -> NavEntry(key) {
                            SearchScreen(application = app)
                        }

                        else -> throw RuntimeException(app.getString(R.string.invalid_navkey, key))
                    }
                }
            )

            MiniPlayerWrapper(
                player = player,
                isPlayerOpened = backStack.last() == PlayerScreenKey,
                onMiniPlayerPressed = { backStack.add(PlayerScreenKey) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .systemBarsPadding()
                    .padding(bottom = 2.dp)
            )
        }
    }
}


fun NavBackStack<NavKey>.safePop() {
    if (this.size > 1) {
        this.removeLastOrNull()
    } else {
        printe("Backstack Pop was called unsafely")
    }
}