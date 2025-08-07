package ca.ilianokokoro.umihi.music.ui.screens.playlists


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.data.repositories.PlaylistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaylistsViewModel() : ViewModel() {
    private val _uiState = MutableStateFlow(PlaylistsState())
    val uiState = _uiState.asStateFlow()

    private val playlistRepository = PlaylistRepository()

    init {
        Log.d("CustomLog", "init PlaylistsViewModel")
        getPlaylists()
    }

    fun getPlaylists() {
        viewModelScope.launch {
            playlistRepository.retrieveAll().collect { apiResult ->
                _uiState.update { screenState ->
                    PlaylistsState(
                        screenState = when (apiResult) {
                            is ApiResult.Error -> ScreenState.Error(apiResult.exception)
                            ApiResult.Loading -> ScreenState.Loading
                            is ApiResult.Success -> ScreenState.LoggedIn(apiResult.data)
                        }
                    )
                }
            }
        }
    }
}