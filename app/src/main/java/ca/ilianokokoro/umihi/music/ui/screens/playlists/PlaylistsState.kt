package ca.ilianokokoro.umihi.music.ui.screens.playlists

import ca.ilianokokoro.umihi.music.models.Playlist


data class PlaylistsState(
    val screenState: ScreenState = ScreenState.Loading
)

sealed class ScreenState {
    data class Success(
        val playlists: List<Playlist>
    ) : ScreenState()

    data object Loading : ScreenState()
    data class Error(val exception: Exception) : ScreenState()
}