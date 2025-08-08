package ca.ilianokokoro.umihi.music.ui.screens.playlist

import ca.ilianokokoro.umihi.music.models.Playlist


data class PlaylistState(
    val screenState: ScreenState = ScreenState.Loading,
    val isRefreshing: Boolean = false
)

sealed class ScreenState {
    data class Success(
        val playlist: Playlist
    ) : ScreenState()

    data object Loading : ScreenState()
    data class Error(val exception: Exception) : ScreenState()
}