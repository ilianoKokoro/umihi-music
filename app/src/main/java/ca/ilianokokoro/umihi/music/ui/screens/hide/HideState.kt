package ca.ilianokokoro.umihi.music.ui.screens.hide

import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.PlaylistInfo


data class HideState(
    val screenState: HideScreenState
)

sealed class HideScreenState {
    data object Loading : HideScreenState()

    data class Success(
        val playlists: List<Playlist>
    ) : HideScreenState()

    data class Error(
        val exception: Exception
    ) : HideScreenState()
}

sealed class ScreenState {
    data class Success(
        val playlist: Playlist
    ) : ScreenState()

    data class Loading(
        val playlistInfo: PlaylistInfo
    ) : ScreenState()

    data class Error(val exception: Exception) : ScreenState()
}