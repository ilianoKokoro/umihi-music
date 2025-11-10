package ca.ilianokokoro.umihi.music.ui.screens.playlist

import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.PlaylistInfo


data class PlaylistState(
    val screenState: ScreenState,
    val isRefreshing: Boolean = false,
    val isDownloading: Boolean = false
)

sealed class ScreenState {
    data class Success(
        val playlist: Playlist
    ) : ScreenState()

    data class Loading(
        val playlistInfo: PlaylistInfo
    ) : ScreenState()

    data class Error(val exception: Exception) : ScreenState()
}