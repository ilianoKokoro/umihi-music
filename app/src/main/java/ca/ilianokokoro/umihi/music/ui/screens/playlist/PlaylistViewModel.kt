package ca.ilianokokoro.umihi.music.ui.screens.playlist


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.Player
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import ca.ilianokokoro.umihi.music.data.repositories.PlaylistRepository
import ca.ilianokokoro.umihi.music.extensions.playPlaylist
import ca.ilianokokoro.umihi.music.extensions.shufflePlaylist
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaylistViewModel(playlist: Playlist, player: Player, application: Application) :
    AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(PlaylistState())
    val uiState = _uiState.asStateFlow()

    private val playlistRepository = PlaylistRepository()
    private val datastoreRepository = DatastoreRepository(application)
    private val _playlist = playlist

    private val _player = player

    init {
        Log.d("CustomLog", "init PlaylistViewModel")
        getPlaylistInfo()
    }

    fun refreshPlaylistInfo() {
        viewModelScope.launch {
            _uiState.update { screenState ->
                _uiState.value.copy(
                    isRefreshing = true
                )
            }
            getPlaylistInfoAsync()
            _uiState.update { screenState ->
                _uiState.value.copy(
                    isRefreshing = false
                )
            }
        }

    }

    fun getPlaylistInfo() {
        viewModelScope.launch {
            getPlaylistInfoAsync()
        }
    }

    fun playPlaylist(startingSong: Song) {
        viewModelScope.launch {
            val playlist = (_uiState.value.screenState as ScreenState.Success).playlist
            _player.playPlaylist(playlist, playlist.songs.indexOf(startingSong))

        }
    }

    fun shufflePlaylist() {
        viewModelScope.launch {
            _player.shufflePlaylist((_uiState.value.screenState as ScreenState.Success).playlist)
        }
    }

    private suspend fun getPlaylistInfoAsync() {
        val cookies = datastoreRepository.getCookies()
        if (!cookies.isEmpty()) {
            playlistRepository.retrieveOne(_playlist, cookies).collect { apiResult ->
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
    }


    companion object {
        fun Factory(
            playlist: Playlist,
            player: Player,
            application: Application
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    PlaylistViewModel(playlist, player, application)
                }
            }
    }
}