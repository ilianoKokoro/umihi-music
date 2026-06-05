package ca.ilianokokoro.umihi.music.ui.screens.home


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printe
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import ca.ilianokokoro.umihi.music.data.repositories.PlaylistRepository
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.Privacy
import ca.ilianokokoro.umihi.music.models.UmihiSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(HomeState())
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
            _uiState.update { currentState ->
                currentState.copy(isRefreshing = true)
            }

            try {
                refreshPlaylistsOnce()
            } catch (ex: Exception) {
                printe(message = ex.toString(), exception = ex)
            } finally {
                _uiState.update { currentState ->
                    currentState.copy(isRefreshing = false)
                }
            }
        }
    }

    private suspend fun refreshPlaylistsOnce() {
        val settings = datastoreRepository.getSettings()

        if (settings.cookies.isEmpty()) {
            _uiState.update { currentState ->
                currentState.copy(screenState = ScreenState.LoggedOut)
            }
            return
        }

        val apiResult = playlistRepository.retrieveAll(settings)
            .first { result ->
                result is ApiResult.Success || result is ApiResult.Error
            }

        val playlists = when (apiResult) {
            is ApiResult.Success -> apiResult.data.toMutableList()

            is ApiResult.Error -> localPlaylistRepository.getAll()
                .map { it.info }
                .toMutableList()

            ApiResult.Loading -> return
        }

        applyPlaylistFiltersAndUpdateState(
            playlists = playlists,
            settings = settings
        )
    }

    suspend fun getPlaylistsSuspend() {
        try {
            val settings = datastoreRepository.getSettings()

            if (settings.cookies.isEmpty()) {
                _uiState.update { currentState ->
                    currentState.copy(screenState = ScreenState.LoggedOut)
                }
                return
            }

            playlistRepository.retrieveAll(settings).collect { apiResult ->
                when (apiResult) {
                    ApiResult.Loading -> {
                        _uiState.update { currentState ->
                            currentState.copy(screenState = ScreenState.Loading)
                        }
                    }

                    is ApiResult.Success -> {
                        val playlists = apiResult.data.toMutableList()

                        applyPlaylistFiltersAndUpdateState(
                            playlists = playlists,
                            settings = settings
                        )
                    }

                    is ApiResult.Error -> {
                        val playlists = localPlaylistRepository.getAll()
                            .map { it.info }
                            .toMutableList()

                        applyPlaylistFiltersAndUpdateState(
                            playlists = playlists,
                            settings = settings
                        )
                    }
                }
            }
        } catch (ex: Exception) {
            printe(message = ex.toString(), exception = ex)
        }
    }

    private fun applyPlaylistFiltersAndUpdateState(
        playlists: MutableList<PlaylistInfo>,
        settings: UmihiSettings
    ) {
        val downloadedPlaylist = PlaylistInfo(
            id = Constants.Downloads.DOWNLOADED_PLAYLIST_ID,
            title = "Downloaded",
        )

        if (!settings.showPodcastPlaylist) {
            playlists.removeIf { it.id == Constants.YoutubeApi.PODCAST_PLAYLIST_ID }
        }

        playlists.add(0, downloadedPlaylist)

        _uiState.update { currentState ->
            currentState.copy(
                screenState = ScreenState.LoggedIn(playlists)
            )
        }
    }

    fun createPlaylist(title: String, description: String, privacy: Privacy) {
        viewModelScope.launch {
            try {
                val settings = datastoreRepository.getSettings()

                if (settings.cookies.isEmpty()) {
                    _uiState.update {
                        it.copy(screenState = ScreenState.LoggedOut)
                    }
                    return@launch
                }

                playlistRepository.create(title, description, privacy, settings)
                    .collect { apiResult ->
                        if (apiResult !is ApiResult.Success || apiResult.data == null) {
                            return@collect
                        }

                        val currentState = _uiState.value.screenState
                        if (currentState !is ScreenState.LoggedIn) {
                            return@collect
                        }

                        val updatedPlaylists = currentState.playlistInfos
                            .toMutableList()
                            .apply {
                                add(index = 2.coerceAtMost(size), element = apiResult.data)
                            }

                        _uiState.update {
                            it.copy(
                                screenState = ScreenState.LoggedIn(updatedPlaylists)
                            )
                        }
                    }
            } catch (ex: Exception) {
                printe(message = ex.toString(), exception = ex)
            }
        }
    }

    fun removePlaylistsFromList(playlistIds: Set<String>) {
        _uiState.update { currentState ->
            val loggedIn = currentState.screenState as? ScreenState.LoggedIn
                ?: return@update currentState

            currentState.copy(
                screenState = loggedIn.copy(
                    playlistInfos = loggedIn.playlistInfos.filterNot { playlist ->
                        playlist.id in playlistIds
                    }
                )
            )
        }
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(application)
            }
        }
    }
}