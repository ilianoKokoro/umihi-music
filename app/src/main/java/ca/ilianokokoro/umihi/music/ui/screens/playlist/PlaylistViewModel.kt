package ca.ilianokokoro.umihi.music.ui.screens.playlist


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.Player
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printe
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import ca.ilianokokoro.umihi.music.data.repositories.DownloadRepository
import ca.ilianokokoro.umihi.music.data.repositories.PlaylistRepository
import ca.ilianokokoro.umihi.music.extensions.playPlaylist
import ca.ilianokokoro.umihi.music.extensions.shufflePlaylist
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaylistViewModel(playlistInfo: PlaylistInfo, player: Player, application: Application) :
    AndroidViewModel(application) {
    private val _playlist = playlistInfo
    private val _uiState = MutableStateFlow(
        PlaylistState(
            screenState = ScreenState.Loading(_playlist)
        )
    )
    val uiState = _uiState.asStateFlow()

    private val playlistRepository = PlaylistRepository()
    private val localPlaylistRepository = AppDatabase.getInstance(application).playlistRepository()
    private val datastoreRepository = DatastoreRepository(application)
    private val downloadRepository = DownloadRepository(application)

    private val _player = player

    init {
        getPlaylistInfo()
    }

    fun refreshPlaylistInfo() {
        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(
                    isRefreshing = true
                )
            }
            getPlaylistInfoAsync()
            _uiState.update {
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

    fun playPlaylist(startingSong: Song? = null) {
        viewModelScope.launch {

            val playlist = (_uiState.value.screenState as ScreenState.Success).playlist
            _player.playPlaylist(
                playlist,
                startingSong?.let { playlist.songs.indexOf(it) } ?: 0
            )
        }
    }

    fun shufflePlaylist() {
        viewModelScope.launch {
            _player.shufflePlaylist((_uiState.value.screenState as ScreenState.Success).playlist)
        }
    }

    fun downloadPlaylist() {
        viewModelScope.launch {
            val playlist = (_uiState.value.screenState as ScreenState.Success).playlist

            _uiState.update {
                _uiState.value.copy(
                    isDownloading = true
                )
            }

            downloadRepository.download(playlist)
            // TODO : only set false when done fr

            _uiState.update {
                _uiState.value.copy(
                    isDownloading = false
                )
            }
        }
    }

    private suspend fun getPlaylistInfoAsync() {
        try {
            val cookies = datastoreRepository.getCookies()
            if (cookies.isEmpty()) {
                throw Exception("Failed to get to login cookies")
            }


            playlistRepository.retrieveOne(Playlist(_playlist), cookies)
                .collect { apiResult ->
                    _uiState.update { _ ->
                        _uiState.value.copy(
                            screenState = when (apiResult) {
                                is ApiResult.Error -> {
                                    getLocalPlaylistInfo()
                                }

                                ApiResult.Loading -> ScreenState.Loading(_playlist)
                                is ApiResult.Success -> ScreenState.Success(apiResult.data)
                            }
                        )
                    }
                }

        } catch (ex: Exception) {
            printe(message = ex.toString(), exception = ex)
            _uiState.update {
                _uiState.value.copy(
                    screenState = ScreenState.Error(ex)
                )
            }
        }

    }


    private suspend fun getLocalPlaylistInfo(): ScreenState {
        try {
            val localPlaylist = localPlaylistRepository.getPlaylistById(_playlist.id)
            if (localPlaylist != null) {
                return ScreenState.Success(localPlaylist)
            }
            throw Exception("Playlist is not fully downloaded")
        } catch (ex: Exception) {
            printe(message = ex.toString(), exception = ex)
            return ScreenState.Error(exception = ex)
        }
    }


    companion object {
        fun Factory(
            playlistInfo: PlaylistInfo,
            player: Player,
            application: Application
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    PlaylistViewModel(playlistInfo, player, application)
                }
            }
    }
}