package ca.ilianokokoro.umihi.music.ui.screens.home


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.LogHelper.printe
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import ca.ilianokokoro.umihi.music.data.repositories.HistoryRepository
import ca.ilianokokoro.umihi.music.data.repositories.PlaylistRepository
import ca.ilianokokoro.umihi.music.models.HomeSection
import ca.ilianokokoro.umihi.music.models.HomeSectionItem
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.Privacy
import ca.ilianokokoro.umihi.music.models.UmihiSettings
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(private val application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(HomeState())
    val uiState = _uiState.asStateFlow()

    private val playlistRepository = PlaylistRepository(application)
    private val datastoreRepository = DatastoreRepository(application)
    private val historyRepository = HistoryRepository(application)

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

    private suspend fun refreshPlaylistsOnce() = coroutineScope {
        val settings = datastoreRepository.getSettings()

        val historyDeferred = async {
            try {
                historyRepository.getRecentSongsList(20)
            } catch (_: Exception) {
                emptyList()
            }
        }

        val sectionsDeferred = async {
            try {
                val result = playlistRepository.retrieveHomeSections(settings)
                    .first { it is ApiResult.Success || it is ApiResult.Error }
                if (result is ApiResult.Success) result.data else emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }

        val playlistsDeferred = async {
            if (settings.cookies.isEmpty()) {
                emptyList()
            } else {
                try {
                    val result = playlistRepository.retrieveAll(settings)
                        .first { it is ApiResult.Success || it is ApiResult.Error }
                    if (result is ApiResult.Success) result.data else emptyList()
                } catch (_: Exception) {
                    emptyList()
                }
            }
        }

        val recentSongs = historyDeferred.await()
        val sections = sectionsDeferred.await().toMutableList()
        val playlists = playlistsDeferred.await()

        if (recentSongs.isNotEmpty()) {
            val historySection = HomeSection(
                id = "recently_played",
                title = application.getString(R.string.recently_played),
                subtitle = null,
                items = recentSongs.map { HomeSectionItem.SongItem(it) }
            )
            sections.add(0, historySection)
        }

        applyFiltersAndUpdateState(
            sections = sections,
            playlists = playlists,
            settings = settings
        )
    }

    suspend fun getPlaylistsSuspend() = coroutineScope {
        try {
            _uiState.update { it.copy(screenState = ScreenState.Loading) }
            val settings = datastoreRepository.getSettings()

            val historyDeferred = async {
                try {
                    historyRepository.getRecentSongsList(20)
                } catch (_: Exception) {
                    emptyList()
                }
            }

            val sectionsDeferred = async {
                try {
                    val result = playlistRepository.retrieveHomeSections(settings)
                        .first { it is ApiResult.Success || it is ApiResult.Error }
                    if (result is ApiResult.Success) result.data else emptyList()
                } catch (e: Exception) {
                    printe(message = "Failed to load home sections: ${e.message}", exception = e)
                    emptyList()
                }
            }

            val playlistsDeferred = async {
                if (settings.cookies.isEmpty()) {
                    emptyList()
                } else {
                    try {
                        val result = playlistRepository.retrieveAll(settings)
                            .first { it is ApiResult.Success || it is ApiResult.Error }
                        if (result is ApiResult.Success) result.data else emptyList()
                    } catch (e: Exception) {
                        printe(message = "Failed to load playlists: ${e.message}", exception = e)
                        emptyList()
                    }
                }
            }

            val recentSongs = historyDeferred.await()
            val sections = sectionsDeferred.await().toMutableList()
            val playlists = playlistsDeferred.await()

            if (recentSongs.isNotEmpty()) {
                val historySection = HomeSection(
                    id = "recently_played",
                    title = application.getString(R.string.recently_played),
                    subtitle = null,
                    items = recentSongs.map { HomeSectionItem.SongItem(it) }
                )
                sections.add(0, historySection)
            }

            applyFiltersAndUpdateState(
                sections = sections,
                playlists = playlists,
                settings = settings
            )
        } catch (ex: Exception) {
            printe(message = ex.toString(), exception = ex)
            _uiState.update { it.copy(screenState = ScreenState.Error(ex)) }
        }
    }

    private fun applyFiltersAndUpdateState(
        sections: List<HomeSection>,
        playlists: List<PlaylistInfo>,
        settings: UmihiSettings
    ) {
        val mutablePlaylists = playlists.toMutableList()
        val downloadedPlaylist = PlaylistInfo(
            id = Constants.Downloads.DOWNLOADED_PLAYLIST_ID,
            title = application.getString(R.string.downloaded),
        )

        if (!settings.showPodcastPlaylist) {
            mutablePlaylists.removeIf { it.id == Constants.YoutubeApi.PODCAST_PLAYLIST_ID }
        }

        mutablePlaylists.add(0, downloadedPlaylist)

        _uiState.update { currentState ->
            currentState.copy(
                screenState = ScreenState.LoggedIn(
                    sections = sections,
                    playlistInfos = mutablePlaylists,
                    isLoggedIn = settings.cookies.isNotEmpty()
                )
            )
        }
    }

    fun createPlaylist(title: String, description: String, privacy: Privacy) {
        viewModelScope.launch {
            try {
                val settings = datastoreRepository.getSettings()

                if (settings.cookies.isEmpty()) {
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
                                screenState = currentState.copy(
                                    playlistInfos = updatedPlaylists
                                )
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