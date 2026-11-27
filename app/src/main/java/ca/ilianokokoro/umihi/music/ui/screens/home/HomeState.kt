package ca.ilianokokoro.umihi.music.ui.screens.home

import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.enums.PlaylistDisplay


data class HomeState(
    val screenState: ScreenState = ScreenState.Loading,
    val isRefreshing: Boolean = false,
    val playlistDisplay: PlaylistDisplay = PlaylistDisplay.CARD,
)

sealed class ScreenState {
    data class LoggedIn(
        val playlistInfos: List<PlaylistInfo>,
        val isLoggedIn: Boolean = false
    ) : ScreenState()

    data object Loading : ScreenState()
    data class Error(val exception: Exception) : ScreenState()
}