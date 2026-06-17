package ca.ilianokokoro.umihi.music.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import kotlinx.serialization.Serializable

@Serializable
data object HomeScreenKey : NavKey

@Serializable
data object SearchScreenKey : NavKey

@Serializable
data object SettingsScreenKey : NavKey

@Serializable
data class PlaylistScreenKey(val playlistInfo: PlaylistInfo) : NavKey

@Serializable
data object AuthScreenKey : NavKey


data class ScreenUiConfig(
    val showBottomBar: Boolean = true,
    val showMiniPlayer: Boolean = true,
    val selectedTab: NavKey? = null
)

@Composable
fun rememberScreenUiConfig(current: NavKey): ScreenUiConfig {
    return remember(current) {
        when (current) {
            HomeScreenKey -> ScreenUiConfig(
                selectedTab = HomeScreenKey
            )

            SearchScreenKey -> ScreenUiConfig(
                selectedTab = SearchScreenKey
            )

            SettingsScreenKey -> ScreenUiConfig(
                selectedTab = SettingsScreenKey
            )

            is PlaylistScreenKey -> ScreenUiConfig(
                showBottomBar = false
            )
            
            AuthScreenKey -> ScreenUiConfig(
                showBottomBar = false,
                showMiniPlayer = false
            )

            else -> ScreenUiConfig()
        }
    }
}

