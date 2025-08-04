package ca.ilianokokoro.umihi.music.ui.screens.playlists


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaylistsViewModel() : ViewModel() {
    private val _uiState = MutableStateFlow(PlaylistsState())
    val uiState = _uiState.asStateFlow()

    init {
        getPlaylists()
    }

    fun getPlaylists() {
        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(
                    screenState = ScreenState.Success(playlists = emptyList()) // TODO
                )
            }
        }
    }
}