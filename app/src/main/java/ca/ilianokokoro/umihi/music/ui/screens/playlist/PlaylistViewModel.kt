package ca.ilianokokoro.umihi.music.ui.screens.playlist


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import ca.ilianokokoro.umihi.music.data.repositories.PlaylistRepository
import ca.ilianokokoro.umihi.music.models.Playlist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaylistViewModel(playlist: Playlist, application: Application) :
    AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(PlaylistState())
    val uiState = _uiState.asStateFlow()

    private val playlistRepository = PlaylistRepository()
    private val datastoreRepository = DatastoreRepository(application)
    val lightPlaylist = playlist


    init {
        Log.d("CustomLog", "init PlaylistViewModel")
        getPlaylistInfo()
    }

    fun getPlaylistInfo() {
        viewModelScope.launch {
            _uiState.update { screenState ->
                _uiState.value.copy(
                    isRefreshing = true
                )
            }

            val cookies = datastoreRepository.getCookies()
            if (!cookies.isEmpty()) {
                playlistRepository.retrieveOne(lightPlaylist, cookies).collect { apiResult ->
                    _uiState.update { screenState ->
                        _uiState.value.copy(
                            screenState = when (apiResult) {
                                is ApiResult.Error -> ScreenState.Error(apiResult.exception)
                                ApiResult.Loading -> ScreenState.Loading
                                is ApiResult.Success -> ScreenState.Success(apiResult.data)
                            }
                        )
                    }
                }
            } else {
                _uiState.update { screenState ->
                    _uiState.value.copy(
                        screenState =
                            ScreenState.Error(Exception("Failed to get to login cookies"))
                    )
                }
            }


            _uiState.update { screenState ->
                _uiState.value.copy(
                    isRefreshing = false
                )
            }
        }
    }


    companion object {
        fun Factory(playlist: Playlist, application: Application): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    PlaylistViewModel(playlist, application)
                }
            }
    }
}