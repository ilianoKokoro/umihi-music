package ca.ilianokokoro.umihi.music.ui.screens.playlist


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.WorkInfo
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printd
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printe
import ca.ilianokokoro.umihi.music.core.managers.PlayerManager
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import ca.ilianokokoro.umihi.music.data.repositories.DownloadRepository
import ca.ilianokokoro.umihi.music.data.repositories.PlaylistRepository
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.navigation.viewmodels.SharedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val playlistInfo: PlaylistInfo,
    private val sharedViewModel: SharedViewModel,
    application: Application
) :
    AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        PlaylistState(
            screenState = ScreenState.Loading(playlistInfo)
        )
    )
    val uiState = _uiState.asStateFlow()

    private val playlistRepository = PlaylistRepository(application)
    private val localPlaylistRepository = AppDatabase.getInstance(application).playlistRepository()
    private val datastoreRepository = DatastoreRepository(application)
    private val downloadRepository = DownloadRepository(application)

    init {
        observeSongDownloads()
        viewModelScope.launch {
            getPlaylistInfoAsync()
            observerDownloadJob()
        }
    }

    private fun observeSongDownloads() {
        viewModelScope.launch {
            localPlaylistRepository.observePlaylistById(playlistInfo.id).collect { localPlaylist ->
                if (localPlaylist != null) {
                    _uiState.update { currentState ->
                        val screenState = currentState.screenState
                        if (screenState is ScreenState.Success) {
                            currentState.copy(
                                screenState = screenState.copy(
                                    playlist = updatePlaylistFrom(
                                        screenState.playlist,
                                        localPlaylist
                                    )
                                )
                            )
                        } else {
                            currentState
                        }
                    }
                }
            }

        }
    }

    suspend fun observerDownloadJob() {
        val playlist = getPlaylist() ?: return
        val existingJobFlow = downloadRepository.getExistingJobFlow(playlist)

        existingJobFlow.collect { workInfos ->
            val workInfo = workInfos.firstOrNull() ?: return@collect

            _uiState.update {
                it.copy(
                    isDownloading =
                        workInfo.state == WorkInfo.State.ENQUEUED ||
                                workInfo.state == WorkInfo.State.RUNNING ||
                                workInfo.state == WorkInfo.State.BLOCKED
                )
            }

            when (workInfo.state) {
                WorkInfo.State.SUCCEEDED -> {
                    printd("Download finished for ${playlist.info.title}")
                }

                WorkInfo.State.FAILED,
                WorkInfo.State.CANCELLED -> {
                    printd("Download failed or cancelled for ${playlist.info.title}")
                }

                else -> {}
            }
        }
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
        val playlist = getPlaylist() ?: return
        viewModelScope.launch {
            PlayerManager.playPlaylist(
                playlist,
                startingSong?.let { playlist.songs.indexOf(it) } ?: 0
            )
        }
    }

    fun shufflePlaylist() {
        val playlist = getPlaylist() ?: return
        viewModelScope.launch {
            PlayerManager.shufflePlaylist(playlist)
        }
    }

    fun downloadPlaylist() {
        val playlist = getPlaylist() ?: return
        viewModelScope.launch {
            if (playlist.downloaded) {
                return@launch
            }

            downloadRepository.downloadPlaylist(playlist)
        }
    }

    fun deletePlaylist(onBack: () -> Unit) {
        viewModelScope.launch {
            try {
                val settings = datastoreRepository.getSettings()
                if (settings.cookies.isEmpty()) {
                    throw Exception("Failed to get to login cookies")
                }

                playlistRepository.delete(playlistInfo, settings)
                    .collect { apiResult ->
                        _uiState.update { _ ->
                            _uiState.value.copy(
                                screenState = when (apiResult) {
                                    is ApiResult.Error -> {
                                        ScreenState.Error(Exception("Failed to delete the playlist"))
                                    }

                                    ApiResult.Loading -> ScreenState.Loading(playlistInfo)
                                    is ApiResult.Success -> {
                                        onBack()
                                        sharedViewModel.markPlaylistDeleted(
                                            playlistInfo
                                        )
                                        ScreenState.Success(Playlist(PlaylistInfo()))
                                    }
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
    }

    fun deleteLocalPlaylist() {
        val playlist = getPlaylist() ?: return
        viewModelScope.launch {
            downloadRepository.deletePlaylist(playlist)
            getPlaylistInfoAsync()
        }
    }

    fun cancelDownload() {
        if (!uiState.value.isDownloading) {
            return
        }
        val playlist = getPlaylist() ?: return
        viewModelScope.launch {
            downloadRepository.cancelPlaylistDownload(playlist)
        }
    }


    fun downloadSong(song: Song) {
        val playlist = getPlaylist() ?: return
        if (song.downloaded) {
            return
        }
        viewModelScope.launch {
            downloadRepository.downloadSong(playlist, song)
        }
    }

    private suspend fun getPlaylistInfoAsync() {
        try {
            val settings = datastoreRepository.getSettings()

            playlistRepository.retrieveOne(Playlist(playlistInfo), settings)
                .collect { apiResult ->
                    _uiState.update { _ ->
                        _uiState.value.copy(
                            screenState = when (apiResult) {
                                is ApiResult.Error -> {
                                    ScreenState.Error(apiResult.exception)
                                }

                                ApiResult.Loading -> ScreenState.Loading(playlistInfo)
                                is ApiResult.Success -> {
                                    ScreenState.Success(playlist = apiResult.data)
                                }
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

    private fun updatePlaylistFrom(oldPlaylist: Playlist, updatedPlaylist: Playlist?): Playlist {
        if (updatedPlaylist == null) {
            return oldPlaylist
        }
        val localMap = updatedPlaylist.songs.associateBy { it.youtubeId }
        val mergedSongs = oldPlaylist.songs.map { remoteSong ->
            localMap[remoteSong.youtubeId] ?: remoteSong
        }
        return oldPlaylist.copy(songs = mergedSongs)
    }

    private fun getPlaylist(): Playlist? {
        val screenState = _uiState.value.screenState
        if (screenState !is ScreenState.Success) {
            return null
        }
        return screenState.playlist
    }

    companion object {
        fun Factory(
            playlistInfo: PlaylistInfo,
            sharedViewModel: SharedViewModel,
            application: Application
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    PlaylistViewModel(playlistInfo, sharedViewModel, application)
                }
            }
    }
}