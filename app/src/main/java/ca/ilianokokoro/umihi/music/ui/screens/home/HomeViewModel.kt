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
    private val songDataSource = ca.ilianokokoro.umihi.music.data.datasources.SongDataSource()

    init {
        getPlaylists()
    }

    fun selectCategory(category: HomeCategory) {
        if (_uiState.value.selectedCategory == category) return
        _uiState.update { it.copy(selectedCategory = category) }
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
        val category = _uiState.value.selectedCategory

        val sectionsDeferred = async { fetchSectionsForCategory(category, settings) }

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

        val sections = sectionsDeferred.await()
        val playlists = playlistsDeferred.await()

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
            val category = _uiState.value.selectedCategory

            val sectionsDeferred = async { fetchSectionsForCategory(category, settings) }

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

            val sections = sectionsDeferred.await()
            val playlists = playlistsDeferred.await()

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

    private suspend fun fetchSectionsForCategory(
        category: HomeCategory,
        settings: UmihiSettings
    ): List<HomeSection> = coroutineScope {
        when (category) {
            HomeCategory.FOR_YOU -> {
                val historyDeferred = async {
                    try { historyRepository.getRecentSongsList(30) } catch (_: Exception) { emptyList() }
                }
                val homeSectionsDeferred = async {
                    try {
                        val res = playlistRepository.retrieveHomeSections(settings)
                            .first { it is ApiResult.Success || it is ApiResult.Error }
                        if (res is ApiResult.Success) res.data else emptyList()
                    } catch (_: Exception) { emptyList() }
                }

                val recentSongs = historyDeferred.await()
                val homeSections = homeSectionsDeferred.await()
                val dynamicSections = mutableListOf<HomeSection>()

                if (recentSongs.isNotEmpty()) {
                    dynamicSections.add(
                        HomeSection(
                            id = "recently_played",
                            title = application.getString(R.string.recently_played),
                            subtitle = null,
                            items = recentSongs.take(15).map { HomeSectionItem.SongItem(it) }
                        )
                    )

                    val topArtists = recentSongs
                        .map { it.artist.trim() }
                        .filter { it.isNotBlank() }
                        .groupingBy { it }
                        .eachCount()
                        .toList()
                        .sortedByDescending { it.second }
                        .map { it.first }
                        .take(2)

                    for (artist in topArtists) {
                        val artistSong = recentSongs.firstOrNull { it.artist.contains(artist, ignoreCase = true) }
                        if (artistSong != null && artistSong.youtubeId.isNotBlank()) {
                            try {
                                val related = songDataSource.getRelatedSongs(artistSong.youtubeId, settings)
                                if (related.isNotEmpty()) {
                                    val mixTitle = String.format(application.getString(R.string.mix_from_artist), artist)
                                    dynamicSections.add(
                                        HomeSection(
                                            id = "mix_$artist",
                                            title = mixTitle,
                                            subtitle = null,
                                            items = related.take(15).map { HomeSectionItem.SongItem(it) }
                                        )
                                    )
                                }
                            } catch (_: Exception) {}
                        }
                    }
                }

                dynamicSections + homeSections
            }

            HomeCategory.CHARTS -> {
                try {
                    val res = playlistRepository.retrieveChartsSections(settings)
                        .first { it is ApiResult.Success || it is ApiResult.Error }
                    if (res is ApiResult.Success && res.data.isNotEmpty()) {
                        res.data
                    } else {
                        emptyList()
                    }
                } catch (_: Exception) {
                    emptyList()
                }
            }

            HomeCategory.CHILL -> {
                try {
                    val res = playlistRepository.retrieveMoodSections(
                        "Chill Acoustic Lofi Relax songs",
                        application.getString(R.string.category_chill),
                        settings
                    ).first { it is ApiResult.Success || it is ApiResult.Error }
                    if (res is ApiResult.Success) res.data else emptyList()
                } catch (_: Exception) { emptyList() }
            }

            HomeCategory.WORKOUT -> {
                try {
                    val res = playlistRepository.retrieveMoodSections(
                        "Workout gym EDM dance energy music",
                        application.getString(R.string.category_workout),
                        settings
                    ).first { it is ApiResult.Success || it is ApiResult.Error }
                    if (res is ApiResult.Success) res.data else emptyList()
                } catch (_: Exception) { emptyList() }
            }

            HomeCategory.FOCUS -> {
                try {
                    val res = playlistRepository.retrieveMoodSections(
                        "Focus study piano classical deep work lofi",
                        application.getString(R.string.category_focus),
                        settings
                    ).first { it is ApiResult.Success || it is ApiResult.Error }
                    if (res is ApiResult.Success) res.data else emptyList()
                } catch (_: Exception) { emptyList() }
            }

            HomeCategory.SLEEP -> {
                try {
                    val res = playlistRepository.retrieveMoodSections(
                        "Sleep rain relax calm bedtime lofi",
                        application.getString(R.string.category_sleep),
                        settings
                    ).first { it is ApiResult.Success || it is ApiResult.Error }
                    if (res is ApiResult.Success) res.data else emptyList()
                } catch (_: Exception) { emptyList() }
            }
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