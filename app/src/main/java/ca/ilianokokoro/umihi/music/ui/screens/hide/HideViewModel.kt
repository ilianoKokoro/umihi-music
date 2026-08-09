package ca.ilianokokoro.umihi.music.ui.screens.hide


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ca.ilianokokoro.umihi.music.core.helpers.LogHelper.printe
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import ca.ilianokokoro.umihi.music.data.repositories.PlaylistRepository
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.ui.navigation.viewmodels.SharedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HideViewModel(
    private val sharedViewModel: SharedViewModel,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        HideState(screenState = HideScreenState.Loading)
    )

    val uiState = _uiState.asStateFlow()
    private val playlistRepository = PlaylistRepository(application)
    private val datastoreRepository = DatastoreRepository(application)

    private val localPlaylistRepository =
        AppDatabase.getInstance(application).playlistRepository()

    init {
        getHiddenPlaylists()

        viewModelScope.launch {
            sharedViewModel.hiddenPlaylistRefreshNeeded.collect { needed ->
                if (needed) {
                    getHiddenPlaylists()
                    sharedViewModel.consumeHiddenPlaylistRefresh()
                }
            }
        }
    }

    fun getHiddenPlaylists() {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(screenState = HideScreenState.Loading)
                }

                val playlists = localPlaylistRepository.fetchHiddenPlaylists()

                _uiState.update {
                    it.copy(
                        screenState = HideScreenState.Success(playlists)
                    )
                }
            } catch (ex: Exception) {
                printe(message = ex.toString(), exception = ex)

                _uiState.update {
                    it.copy(
                        screenState = HideScreenState.Error(ex)
                    )
                }
            }
        }
    }

    fun unhidePlaylist(playlist: PlaylistInfo) {
        viewModelScope.launch {
            localPlaylistRepository.setPlaylistVisibility(
                playlistId = playlist.id,
                hidden = false
            )
            sharedViewModel.requestPlaylistRefresh()
            getHiddenPlaylists()
        }
    }

    fun deletePlaylist(playlist: PlaylistInfo) {
        viewModelScope.launch {
            try {
                val settings = datastoreRepository.getSettings()

                playlistRepository.delete(playlist, settings).collect { result ->
                    if (result is ApiResult.Success) {
                        getHiddenPlaylists()
                        sharedViewModel.requestPlaylistRefresh()
                    }
                }
            } catch (ex: Exception) {
                printe(message = ex.toString(), exception = ex)
            }
        }
    }

    companion object {
        fun Factory(
            sharedViewModel: SharedViewModel,
            application: Application
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    HideViewModel(
                        sharedViewModel = sharedViewModel,
                        application = application
                    )
                }
            }
    }
}