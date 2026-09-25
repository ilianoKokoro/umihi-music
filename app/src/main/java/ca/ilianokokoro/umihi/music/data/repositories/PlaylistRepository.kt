package ca.ilianokokoro.umihi.music.data.repositories

import android.app.Application
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.LogHelper.printe
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.data.datasources.PlaylistDataSource
import ca.ilianokokoro.umihi.music.extensions.toException
import ca.ilianokokoro.umihi.music.models.AddToPlaylistOption
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.Privacy
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.models.UmihiSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.uuid.Uuid

class PlaylistRepository(application: Application) {
    private val context = application
    private val playlistDataSource = PlaylistDataSource()
    private val localPlaylistDataSource = AppDatabase.getInstance(application).playlistRepository()
    private val localSongDataSource = AppDatabase.getInstance(application).songRepository()

    suspend fun getDownloadedSongsCount(): Int =
        localSongDataSource.countDownloadedSongs()

    fun getDownloadedSongsCountFlow(): Flow<Int> =
        localSongDataSource.countDownloadedSongsFlow()

    fun retrieveAll(settings: UmihiSettings): Flow<ApiResult<List<PlaylistInfo>>> {
        return flow {
            emit(ApiResult.Loading)
            if (settings.offlineMode) {
                val localPlaylists = localPlaylistDataSource
                    .fetchVisiblePlaylists()
                    .filter { playlist -> playlist.songs.any { it.downloaded } }
                    .map { it.toLocalPlaylistInfo() }
                emit(ApiResult.Success(localPlaylists))
                return@flow
            }
            try {
                val remotePlaylists = playlistDataSource.retrieveAll(settings)
                val hiddenIds = localPlaylistDataSource
                    .fetchHiddenPlaylists()
                    .map { it.info.id }
                    .toSet()
                emit(
                    ApiResult.Success(
                        remotePlaylists.filter { it.id !in hiddenIds }
                    )
                )
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }
                val localPlaylists = localPlaylistDataSource.fetchVisiblePlaylists().map { it.toLocalPlaylistInfo() }
                emit(ApiResult.Success(localPlaylists))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun retrieveOne(
        playlist: Playlist,
        settings: UmihiSettings,
        onProgress: (Int) -> Unit = {}
    ): Flow<ApiResult<Playlist>> {
        return flow {
            emit(ApiResult.Loading)

            if (playlist.info.id == Constants.Downloads.DOWNLOADED_PLAYLIST_ID) {
                val downloadedSongs = localSongDataSource.getDownloadedSongs()
                emit(ApiResult.Success(Playlist(info = playlist.info, songs = downloadedSongs)))
                return@flow
            }

            if (settings.offlineMode) {
                val localPlaylist = localPlaylistDataSource.getPlaylistById(playlist.info.id)
                if (localPlaylist != null) {
                    emit(
                        ApiResult.Success(
                            localPlaylist.copy(
                                songs = localPlaylist.songs.filter { it.downloaded }
                            )
                        )
                    )
                } else {
                    emit(
                        ApiResult.Error(
                            Exception(context.getString(R.string.playlist_not_downloaded))
                        )
                    )
                }
                return@flow
            }

            try {
                val remotePlaylist = playlistDataSource.retrieveOne(playlist, settings, onProgress)
                val localPlaylist = localPlaylistDataSource.getPlaylistById(playlist.info.id)
                val mergedPlaylist = mergeWithLocal(remotePlaylist, localPlaylist)
                if (localPlaylist != null && localPlaylist.info.shouldBeDownloaded) {
                    try {
                        syncLocalPlaylist(mergedPlaylist)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        printe(message = "Error syncing local playlist", exception = e)
                    }
                }
                emit(ApiResult.Success(mergedPlaylist))
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }
                val localPlaylist = localPlaylistDataSource.getPlaylistById(playlist.info.id)
                if (localPlaylist != null) {
                    emit(
                        ApiResult.Success(
                            localPlaylist.copy(songs = localPlaylist.songs.filter { it.downloaded })
                        )
                    )
                } else {
                    emit(ApiResult.Error(e.toException()))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    fun create(
        title: String,
        description: String,
        privacy: Privacy,
        settings: UmihiSettings
    ): Flow<ApiResult<PlaylistInfo?>> {
        return flow {
            emit(ApiResult.Loading)
            emit(
                ApiResult.Success(
                    playlistDataSource.create(title, description, privacy, settings)
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    fun addSongToPlaylist(
        playlistId: String,
        songId: String,
        settings: UmihiSettings
    ): Flow<ApiResult<Unit>> {
        return flow {
            emit(ApiResult.Loading)
            emit(
                ApiResult.Success(
                    playlistDataSource.edit(
                        playlistId = playlistId,
                        settings = settings,
                        videoIdsToAdd = listOf(songId)
                    )
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    fun removeSongFromPlaylist(
        playlistId: String,
        videoId: String,
        setVideoId: String?,
        settings: UmihiSettings
    ): Flow<ApiResult<Unit>> {
        return flow {
            emit(ApiResult.Loading)
            emit(
                ApiResult.Success(
                    playlistDataSource.edit(
                        playlistId = playlistId,
                        settings = settings,
                        videosToRemove = listOf(videoId to setVideoId)
                    )
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    fun retrieveAddToPlaylistOptions(
        videoId: String,
        settings: UmihiSettings
    ): Flow<ApiResult<List<AddToPlaylistOption>>> {
        return flow {
            emit(ApiResult.Loading)
            emit(
                ApiResult.Success(
                    playlistDataSource.retrieveAddToPlaylistOptions(videoId, settings)
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    fun toggleSongInPlaylist(
        playlistId: String,
        song: Song,
        settings: UmihiSettings,
        currentlyContains: Boolean,
        useSongSetVideoId: Boolean = false,
    ): Flow<ApiResult<Unit>> {
        return flow {
            emit(ApiResult.Loading)
            if (currentlyContains) {
                val setVideoId = (if (useSongSetVideoId) song.setVideoId else null)
                    ?: playlistDataSource.findSetVideoId(playlistId, song.youtubeId, settings)
                playlistDataSource.edit(
                    playlistId = playlistId,
                    settings = settings,
                    videosToRemove = listOf(song.youtubeId to setVideoId),
                )
            } else {
                playlistDataSource.edit(
                    playlistId = playlistId,
                    settings = settings,
                    videoIdsToAdd = listOf(song.youtubeId),
                )
            }
            emit(ApiResult.Success(Unit))
        }.flowOn(Dispatchers.IO)
    }

    fun delete(
        playlist: PlaylistInfo,
        settings: UmihiSettings
    ): Flow<ApiResult<Unit>> {
        return flow {
            emit(ApiResult.Loading)
            emit(ApiResult.Success(playlistDataSource.delete(playlist, settings)))
        }.flowOn(Dispatchers.IO)
    }

    fun removeFromLibrary(
        playlist: PlaylistInfo,
        settings: UmihiSettings
    ): Flow<ApiResult<Unit>> {
        return flow {
            emit(ApiResult.Loading)
            emit(ApiResult.Success(playlistDataSource.removeFromLibrary(playlist, settings)))
        }.flowOn(Dispatchers.IO)
    }

    fun hidePlaylist(playlist: PlaylistInfo): Flow<ApiResult<Unit>> {
        return flow {
            emit(ApiResult.Loading)

            localPlaylistDataSource.insertPlaylist(
                playlist.copy(
                    hidden = true,
                    coverPath = playlist.coverPath ?: getStoredCoverPath(playlist.id)
                )
            )

            emit(ApiResult.Success(Unit))
        }.flowOn(Dispatchers.IO)
    }

    fun unhidePlaylist(playlist: PlaylistInfo): Flow<ApiResult<Unit>> {
        return flow {
            emit(ApiResult.Loading)

            localPlaylistDataSource.insertPlaylist(
                playlist.copy(
                    hidden = false,
                    coverPath = playlist.coverPath ?: getStoredCoverPath(playlist.id)
                )
            )

            emit(ApiResult.Success(Unit))
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun getStoredCoverPath(playlistId: String): String? =
        localPlaylistDataSource.getPlaylistById(playlistId)?.info?.coverPath

    fun edit(
        playlistId: String,
        settings: UmihiSettings,
        title: String? = null,
        description: String? = null,
        privacy: Privacy? = null,
        videoIdsToAdd: List<String>? = null,
        videosToRemove: List<Pair<String, String?>>? = null,
    ): Flow<ApiResult<Unit>> {
        return flow {
            emit(ApiResult.Loading)
            emit(
                ApiResult.Success(
                    playlistDataSource.edit(
                        playlistId = playlistId,
                        settings = settings,
                        title = title,
                        description = description,
                        privacy = privacy,
                        videoIdsToAdd = videoIdsToAdd,
                        videosToRemove = videosToRemove,
                    )
                )
            )
        }.flowOn(Dispatchers.IO)
    }
    private fun Playlist.toLocalPlaylistInfo(): PlaylistInfo =
        info.apply { songCount = songs.count { it.downloaded } }

    private fun mergeWithLocal(remotePlaylist: Playlist, localPlaylist: Playlist?): Playlist {
        if (localPlaylist == null) {
            return remotePlaylist
        }
        val localMap = localPlaylist.songs.associateBy { it.youtubeId }
        val mergedSongs = remotePlaylist.songs.map { remoteSong ->
            val localCopy = localMap[remoteSong.youtubeId]?.copy(uid = Uuid.random().toString())
            if (localCopy != null) {
                remoteSong.setVideoId?.let { localCopy.setVideoId = it }
                localCopy
            } else {
                remoteSong
            }
        }
        return remotePlaylist.copy(
            info = remotePlaylist.info.copy(
                hidden = localPlaylist.info.hidden,
                shouldBeDownloaded = localPlaylist.info.shouldBeDownloaded,
                coverPath = localPlaylist.info.coverPath ?: remotePlaylist.info.coverPath
            ),
            songs = mergedSongs
        )
    }

    private suspend fun syncLocalPlaylist(playlist: Playlist) {
        val mergedSongs = playlist.songs
        if (mergedSongs.isEmpty()) {
            localPlaylistDataSource.syncPlaylistWithSongs(playlist)
            return
        }

        val savedSongs = localSongDataSource
            .getSongsByYoutubeIds(mergedSongs.map { it.youtubeId })
            .associateBy { it.youtubeId }

        val preservedSongs = mergedSongs.map { song ->
            val saved = savedSongs[song.youtubeId] ?: return@map song
            song.copy(
                thumbnailPath = saved.thumbnailPath ?: song.thumbnailPath,
                audioFilePath = saved.audioFilePath ?: song.audioFilePath,
                streamUrl = saved.streamUrl ?: song.streamUrl,
                isLiked = saved.isLiked ?: song.isLiked,
            )
        }

        localPlaylistDataSource.syncPlaylistWithSongs(playlist.copy(songs = preservedSongs))
    }
}
