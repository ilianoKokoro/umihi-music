package ca.ilianokokoro.umihi.music.ui.screens.playlists

import ca.ilianokokoro.umihi.music.models.PlaylistInfo


data class PlaylistsState(
    val screenState: ScreenState = ScreenState.Loading,
    val isRefreshing: Boolean = false
)

sealed class ScreenState {
    data class LoggedIn(
        val playlistInfos: List<PlaylistInfo>
    ) : ScreenState()


    data object LoggedOut
        : ScreenState()

    data object Loading : ScreenState()
    data class Error(val exception: Exception) : ScreenState()
}