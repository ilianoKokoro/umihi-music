package ca.ilianokokoro.umihi.music.ui.screens.playlists


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printe
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
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
    private val localPlaylistRepository = AppDatabase.getInstance(application).playlistRepository()
    private val datastoreRepository = DatastoreRepository(application)


    init {
        getPlaylists()
    }

    fun getPlaylists() {
        viewModelScope.launch {
            getPlaylistsSuspend()
        }
    }

    fun refreshPlaylists() {
        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(
                    isRefreshing = true
                )
            }

            getPlaylistsSuspend()

            _uiState.update {
                _uiState.value.copy(
                    isRefreshing = false
                )
            }
        }
    }

    suspend fun getPlaylistsSuspend() {
        try {
            val cookies = datastoreRepository.getCookies()
            if (!cookies.isEmpty()) {
                playlistRepository.retrieveAll(cookies).collect { apiResult ->
                    _uiState.update {
                        _uiState.value.copy(
                            screenState = when (apiResult) {
                                is ApiResult.Error -> { // TODO : Maybe still add a message
                                    ScreenState.LoggedIn(
                                        localPlaylistRepository.getAll().map { it.info })
                                }

                                ApiResult.Loading -> ScreenState.Loading
                                is ApiResult.Success -> ScreenState.LoggedIn(apiResult.data)
                            }
                        )
                    }
                }

            } else {
                _uiState.update {
                    _uiState.value.copy(
                        screenState =
                            ScreenState.LoggedOut
                    )
                }
            }
        } catch (ex: Exception) {
            printe(message = ex.toString(), exception = ex)
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