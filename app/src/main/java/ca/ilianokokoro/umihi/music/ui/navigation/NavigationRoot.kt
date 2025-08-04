package ca.ilianokokoro.umihi.music.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import ca.ilianokokoro.umihi.music.ui.screens.playlists.PlaylistsScreen
import ca.ilianokokoro.umihi.music.ui.screens.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable
data object PlaylistsScreen : NavKey

@Serializable
data object SettingsScreen : NavKey

@Serializable
data class PlaylistScreen(val id: Int) : NavKey

@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(PlaylistsScreen) // Start screen
    val zoomDuration = 200
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        entryDecorators = listOf(
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
            rememberSceneSetupNavEntryDecorator()
        ), transitionSpec = {
            // Zoom in when navigating forward
            scaleIn(animationSpec = tween(zoomDuration), initialScale = 0.85f) togetherWith
                    scaleOut(animationSpec = tween(zoomDuration), targetScale = 1.1f)
        },

        popTransitionSpec = {
            // Zoom out when navigating back
            scaleIn(animationSpec = tween(zoomDuration), initialScale = 1.1f) togetherWith
                    scaleOut(animationSpec = tween(zoomDuration), targetScale = 0.85f)
        },

        predictivePopTransitionSpec = {
            // Optional: match pop transition
            scaleIn(animationSpec = tween(zoomDuration), initialScale = 1.1f) togetherWith
                    scaleOut(animationSpec = tween(zoomDuration), targetScale = 0.85f)
        }, entryProvider = { key ->

            when (key) {
                is PlaylistsScreen -> {
                    NavEntry(key = key) {
                        PlaylistsScreen(onSettingsButtonPress = {
                            backStack.add(SettingsScreen)
                        })
                    }
                }

                is SettingsScreen -> {
                    NavEntry(key = key) {
                        SettingsScreen(onBack = {
                            backStack.removeLastOrNull()
                        })
                    }
                }

                is PlaylistScreen -> {
                    NavEntry(key = key) {
                        //PlaylistScreen()
                    }
                }

                else -> throw RuntimeException("Invalid NavKey : $key")
            }
        }

    )


}