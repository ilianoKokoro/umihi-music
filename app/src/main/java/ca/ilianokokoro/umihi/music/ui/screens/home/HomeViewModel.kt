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

    private fun getTimeGreeting(): Pair<Int, String> {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> R.string.greeting_morning to "☀️"
            in 12..17 -> R.string.greeting_afternoon to "🌤️"
            in 18..22 -> R.string.greeting_evening to "🌆"
            else -> R.string.greeting_night to "🌙"
        }
    }

    private suspend fun fetchContextualTimeShelf(settings: UmihiSettings): List<HomeSection> {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val (query, titleRes) = when (hour) {
            in 5..11 -> "Acoustic Pop Morning Coffee Chill Songs" to R.string.context_morning_title
            in 12..17 -> "Deep Focus Study Piano Work Lofi Beats" to R.string.context_afternoon_title
            in 18..22 -> "Evening Wind Down Chillout Pop Songs" to R.string.context_evening_title
            else -> "Night Sleep Rain Lofi Bedtime Relax Music" to R.string.context_night_title
        }
        return try {
            val res = playlistRepository.retrieveMoodSections(query, application.getString(titleRes), settings)
                .first { it is ApiResult.Success || it is ApiResult.Error }
            if (res is ApiResult.Success) res.data else emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchSectionsForCategory(
        category: HomeCategory,
        settings: UmihiSettings
    ): List<HomeSection> = coroutineScope {
        val (greetingRes, greetingEmoji) = getTimeGreeting()
        _uiState.update { it.copy(timeGreetingRes = greetingRes, timeGreetingEmoji = greetingEmoji) }

        when (category) {
            HomeCategory.FOR_YOU -> {
                val historyDeferred = async {
                    try { historyRepository.getRecentSongsList(50) } catch (_: Exception) { emptyList() }
                }
                val homeSectionsDeferred = async {
                    try {
                        val res = playlistRepository.retrieveHomeSections(settings)
                            .first { it is ApiResult.Success || it is ApiResult.Error }
                        if (res is ApiResult.Success) res.data else emptyList()
                    } catch (_: Exception) { emptyList() }
                }
                val contextualDeferred = async {
                    fetchContextualTimeShelf(settings)
                }
                val trendingShelfDeferred = async {
                    try {
                        val res = playlistRepository.retrieveMoodSections(
                            "Trending Viral TikTok Hits Vietnam Pop",
                            application.getString(R.string.trending_tiktok_title),
                            settings
                        ).first { it is ApiResult.Success || it is ApiResult.Error }
                        if (res is ApiResult.Success) res.data else emptyList()
                    } catch (_: Exception) { emptyList() }
                }

                val recentSongs = historyDeferred.await()
                val homeSections = homeSectionsDeferred.await()
                val contextualSections = contextualDeferred.await()
                val trendingSections = trendingShelfDeferred.await()

                val dynamicSections = mutableListOf<HomeSection>()

                if (recentSongs.isNotEmpty()) {
                    // 1. Recently Played (up to 15 songs)
                    dynamicSections.add(
                        HomeSection(
                            id = "recently_played",
                            title = application.getString(R.string.recently_played),
                            subtitle = null,
                            items = recentSongs.take(15).map { HomeSectionItem.SongItem(it) }
                        )
                    )

                    // 2. Favorite Artists Shelf
                    val artistCounts = recentSongs
                        .map { it.artist.trim() }
                        .filter { it.isNotBlank() }
                        .groupingBy { it }
                        .eachCount()
                        .toList()
                        .sortedByDescending { it.second }
                        .take(6)

                    if (artistCounts.isNotEmpty()) {
                        val artistItems = artistCounts.mapNotNull { (artistName, count) ->
                            val representativeSong = recentSongs.firstOrNull { it.artist.contains(artistName, ignoreCase = true) }
                            representativeSong?.let {
                                HomeSectionItem.ArtistItem(
                                    name = artistName,
                                    thumbnailHref = it.thumbnailPath ?: it.thumbnailHref,
                                    songCount = count
                                )
                            }
                        }
                        if (artistItems.isNotEmpty()) {
                            dynamicSections.add(
                                HomeSection(
                                    id = "favorite_artists",
                                    title = application.getString(R.string.favorite_artists_title),
                                    subtitle = null,
                                    items = artistItems
                                )
                            )
                        }
                    }

                    // 3. Daily Mix 1, 2, 3
                    val top3Artists = artistCounts.take(3).map { it.first }
                    for ((index, artist) in top3Artists.withIndex()) {
                        val artistSong = recentSongs.firstOrNull { it.artist.contains(artist, ignoreCase = true) }
                        if (artistSong != null && artistSong.youtubeId.isNotBlank()) {
                            try {
                                val related = songDataSource.getRelatedSongs(artistSong.youtubeId, settings)
                                if (related.isNotEmpty()) {
                                    val mixTitle = String.format(application.getString(R.string.daily_mix_title), index + 1) + " • $artist"
                                    val artistOwnSongs = recentSongs.filter { it.artist.contains(artist, ignoreCase = true) }.take(5)
                                    val blended = (artistOwnSongs + related.filter { rel -> artistOwnSongs.none { it.youtubeId == rel.youtubeId } }).distinctBy { it.youtubeId }.take(20)
                                    dynamicSections.add(
                                        HomeSection(
                                            id = "daily_mix_${index + 1}",
                                            title = mixTitle,
                                            subtitle = application.getString(R.string.supermix_subtitle),
                                            items = blended.map { HomeSectionItem.SongItem(it) }
                                        )
                                    )
                                }
                            } catch (_: Exception) {}
                        }
                    }

                    // 4. Forgotten Favorites (older songs from history)
                    if (recentSongs.size > 15) {
                        val olderSongs = recentSongs.drop(12).take(15)
                        if (olderSongs.isNotEmpty()) {
                            dynamicSections.add(
                                HomeSection(
                                    id = "forgotten_favorites",
                                    title = application.getString(R.string.forgotten_favorites_title),
                                    subtitle = application.getString(R.string.forgotten_favorites_subtitle),
                                    items = olderSongs.map { HomeSectionItem.SongItem(it) }
                                )
                            )
                        }
                    }
                }

                // 5. Add Contextual Time Shelf (Coffee morning / Deep focus / Night chill)
                dynamicSections.addAll(contextualSections)

                // 6. Add Trending / Themed Shelf
                dynamicSections.addAll(trendingSections)

                // 7. Add YouTube Music official recommendation sections
                dynamicSections + homeSections
            }

            HomeCategory.CHARTS -> {
                try {
                    val res = playlistRepository.retrieveChartsSections(settings)
                        .first { it is ApiResult.Success || it is ApiResult.Error }
                    val rawSections = if (res is ApiResult.Success && res.data.isNotEmpty()) res.data else emptyList()
                    rawSections.map { section ->
                        val rankedItems = section.items.mapIndexed { idx, item ->
                            if (item is HomeSectionItem.SongItem) {
                                item.copy(rank = idx + 1)
                            } else {
                                item
                            }
                        }
                        section.copy(items = rankedItems)
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