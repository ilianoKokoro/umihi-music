package ca.ilianokokoro.umihi.music.ui.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import ca.ilianokokoro.umihi.music.ui.screens.PlaylistsScreen
import ca.ilianokokoro.umihi.music.ui.screens.SettingsScreen
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
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        entryDecorators = listOf(
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
            rememberSceneSetupNavEntryDecorator()
        ), transitionSpec = {
            // Slide in from right when navigating forward
            slideInHorizontally(initialOffsetX = { it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { -it })
        },
        popTransitionSpec = {
            // Slide in from left when navigating back
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        },
        predictivePopTransitionSpec = {
            // Slide in from left when navigating back
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
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