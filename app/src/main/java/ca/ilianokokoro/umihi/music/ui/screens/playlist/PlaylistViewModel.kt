package ca.ilianokokoro.umihi.music.ui.screens.playlist


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.Player
import androidx.work.WorkInfo
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printd
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
        viewModelScope.launch {
            getPlaylistInfoAsync()
            observerDownloadJob()
        }
    }

    suspend fun observerDownloadJob() {
        val playlist = (_uiState.value.screenState as ScreenState.Success).playlist
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
            downloadRepository.download(playlist)
            observerDownloadJob()
        }
    }

    private suspend fun getPlaylistInfoAsync() {
        try {
            val localPlaylist = localPlaylistRepository.getPlaylistById(_playlist.id)

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
                                    if (localPlaylist == null) {
                                        ScreenState.Error(Exception("Playlist is not downloaded"))
                                    } else {
                                        ScreenState.Success(playlist = localPlaylist)
                                    }
                                }

                                ApiResult.Loading -> ScreenState.Loading(_playlist)
                                is ApiResult.Success -> {
                                    var remotePlaylist = apiResult.data

                                    if (localPlaylist != null) {
                                        val localMap =
                                            localPlaylist.songs.associateBy { it.youtubeId }

                                        val updatedSongs = remotePlaylist.songs.map { remoteSong ->
                                            localMap[remoteSong.youtubeId] ?: remoteSong
                                        }

                                        remotePlaylist =
                                            remotePlaylist.copy(songs = updatedSongs)
                                    }
                                    ScreenState.Success(playlist = remotePlaylist)
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