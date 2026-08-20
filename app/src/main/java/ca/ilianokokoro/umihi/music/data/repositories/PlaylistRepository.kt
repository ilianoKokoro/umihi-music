package ca.ilianokokoro.umihi.music.data.repositories

import android.app.Application
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.data.datasources.PlaylistDataSource
import ca.ilianokokoro.umihi.music.extensions.toException
import ca.ilianokokoro.umihi.music.models.HomeSection
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.Privacy
import ca.ilianokokoro.umihi.music.models.UmihiSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.uuid.Uuid

class PlaylistRepository(application: Application) {
    private val playlistDataSource = PlaylistDataSource()
    private val localPlaylistDataSource = AppDatabase.getInstance(application).playlistRepository()
    private val localSongDataSource = AppDatabase.getInstance(application).songRepository()

    fun retrieveHomeSections(settings: UmihiSettings): Flow<ApiResult<List<HomeSection>>> {
        return flow {
            emit(ApiResult.Loading)
            try {
                val sections = playlistDataSource.retrieveHomeSections(settings)
                emit(ApiResult.Success(sections))
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }
                emit(ApiResult.Error(e.toException()))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun retrieveAll(settings: UmihiSettings): Flow<ApiResult<List<PlaylistInfo>>> {
        return flow {
            emit(ApiResult.Loading)
            try {
                val remotePlaylists = playlistDataSource.retrieveAll(settings)
                emit(ApiResult.Success(remotePlaylists))
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }
                val localPlaylists = localPlaylistDataSource.getAll().map { it.info }
                emit(ApiResult.Success(localPlaylists))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun retrieveOne(
        playlist: Playlist,
        settings: UmihiSettings
    ): Flow<ApiResult<Playlist>> {
        return flow {
            emit(ApiResult.Loading)

            if (playlist.info.id == Constants.Downloads.DOWNLOADED_PLAYLIST_ID) {
                val downloadedSongs = localSongDataSource.getDownloadedSongs()
                emit(ApiResult.Success(Playlist(info = playlist.info, songs = downloadedSongs)))
                return@flow
            }

            try {
                val remotePlaylist = playlistDataSource.retrieveOne(playlist, settings)
                val localPlaylist = localPlaylistDataSource.getPlaylistById(playlist.info.id)
                emit(ApiResult.Success(mergeWithLocal(remotePlaylist, localPlaylist)))
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
        setVideoId: String,
        settings: UmihiSettings
    ): Flow<ApiResult<Unit>> {
        return flow {
            emit(ApiResult.Loading)
            emit(
                ApiResult.Success(
                    playlistDataSource.edit(
                        playlistId = playlistId,
                        settings = settings,
                        setVideoIdsToRemove = listOf(setVideoId)
                    )
                )
            )
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

    fun edit(
        playlistId: String,
        settings: UmihiSettings,
        title: String? = null,
        description: String? = null,
        privacy: Privacy? = null,
        videoIdsToAdd: List<String>? = null,
        setVideoIdsToRemove: List<String>? = null,
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
                        setVideoIdsToRemove = setVideoIdsToRemove,
                    )
                )
            )
        }.flowOn(Dispatchers.IO)
    }
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
        return remotePlaylist.copy(songs = mergedSongs)
    }
}
