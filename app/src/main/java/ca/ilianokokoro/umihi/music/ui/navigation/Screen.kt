package ca.ilianokokoro.umihi.music.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import ca.ilianokokoro.umihi.music.R
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

@Serializable
data object PlayerScreenKey : NavKey


data class ScreenUiConfig(
    val title: String = String(),
    @param:StringRes val titleId: Int = 0,
    val showBack: Boolean = false,
    val showBottomBar: Boolean = true,
    val selectedTab: NavKey? = null
)

@Composable
fun rememberScreenUiConfig(current: NavKey): ScreenUiConfig {
    return remember(current) {
        when (current) {
            HomeScreenKey -> ScreenUiConfig(
                titleId = R.string.home,
                showBack = false,
                showBottomBar = true,
                selectedTab = HomeScreenKey
            )

            SearchScreenKey -> ScreenUiConfig(
                titleId = R.string.search,
                showBack = false,
                showBottomBar = true,
                selectedTab = SearchScreenKey
            )

            SettingsScreenKey -> ScreenUiConfig(
                titleId = R.string.settings,
                showBack = false,
                showBottomBar = true,
                selectedTab = SettingsScreenKey
            )

            is PlaylistScreenKey -> ScreenUiConfig(
                title = current.playlistInfo.title,
                showBack = true,
                showBottomBar = false
            )

            PlayerScreenKey -> ScreenUiConfig(
                showBack = true,
                showBottomBar = false
            )

            AuthScreenKey -> ScreenUiConfig(
                titleId = R.string.log_in,
                showBack = true,
                showBottomBar = false
            )

            else -> ScreenUiConfig()
        }
    }
}

