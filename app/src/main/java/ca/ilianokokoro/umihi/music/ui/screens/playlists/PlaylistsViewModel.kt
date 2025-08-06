package ca.ilianokokoro.umihi.music.ui.screens.playlists


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.ilianokokoro.umihi.music.models.Playlist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaylistsViewModel() : ViewModel() {
    private val _uiState = MutableStateFlow(PlaylistsState())
    val uiState = _uiState.asStateFlow()

    init {
        Log.d("PlaylistsViewModel", "init")
        getPlaylists()
    }

    fun getPlaylists() {
        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(
                    screenState = ScreenState.LoggedIn(
                        playlists = List(12) { index ->
                            Playlist(
                                id = "",
                                title = "Playlist #${index + 1} " + "🎵".repeat((index % 3) + 1),
                                coverHref = "https://upload.wikimedia.org/wikipedia/commons/thumb/b/bd/Test.svg/1280px-Test.svg.png"
                            )
                        }
                    ) // TODO
                )
            }
        }
    }
}