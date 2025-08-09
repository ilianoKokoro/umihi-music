package ca.ilianokokoro.umihi.music.ui.screens.playlists


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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaylistsViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(PlaylistsState())
    val uiState = _uiState.asStateFlow()

    private val playlistRepository = PlaylistRepository()
    private val datastoreRepository = DatastoreRepository(application)


    init {
        Log.d("CustomLog", "init PlaylistsViewModel")
        getPlaylists()
    }


    fun getPlaylists() {
        viewModelScope.launch {
            getPlaylistsSuspend()
        }
    }

    fun refreshPlaylists() {
        viewModelScope.launch {
            _uiState.update { screenState ->
                _uiState.value.copy(
                    isRefreshing = true
                )
            }
            getPlaylistsSuspend()
            _uiState.update { screenState ->
                _uiState.value.copy(
                    isRefreshing = false
                )
            }
        }
    }

    suspend fun getPlaylistsSuspend() {
        Log.d("CustomLog", "getting playlists getPlaylists")

        val cookies = datastoreRepository.getCookies()
        if (!cookies.isEmpty()) {
            playlistRepository.retrieveAll(cookies).collect { apiResult ->
                _uiState.update { screenState ->
                    _uiState.value.copy(
                        screenState = when (apiResult) {
                            is ApiResult.Error -> ScreenState.Error(apiResult.exception)
                            ApiResult.Loading -> ScreenState.Loading
                            is ApiResult.Success -> ScreenState.LoggedIn(apiResult.data)
                        }
                    )
                }
            }

        } else {
            _uiState.update { screenState ->
                _uiState.value.copy(
                    screenState =
                        ScreenState.LoggedOut
                )
            }
        }
    }


    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PlaylistsViewModel(application)
            }
        }
    }
}