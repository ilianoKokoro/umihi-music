package ca.ilianokokoro.umihi.music.data.repositories

import android.app.Application
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.data.datasources.PlaylistDataSource
import ca.ilianokokoro.umihi.music.extensions.toException
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.Privacy
import ca.ilianokokoro.umihi.music.models.UmihiSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.UUID

class PlaylistRepository(application: Application) {
    private val playlistDataSource = PlaylistDataSource()
    private val localPlaylistDataSource = AppDatabase.getInstance(application).playlistRepository()
    private val localSongDataSource = AppDatabase.getInstance(application).songRepository()

    fun retrieveAll(settings: UmihiSettings): Flow<ApiResult<List<PlaylistInfo>>> {
        return flow {
            emit(ApiResult.Loading)
            try {
                val remotePlaylists = playlistDataSource.retrieveAll(settings)
                emit(ApiResult.Success(remotePlaylists))
            } catch (e: Exception) {
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

    fun delete(
        playlist: PlaylistInfo,
        settings: UmihiSettings
    ): Flow<ApiResult<Unit>> {
        return flow {
            emit(ApiResult.Loading)
            emit(ApiResult.Success(playlistDataSource.delete(playlist, settings)))
        }.flowOn(Dispatchers.IO)
    }

    private fun mergeWithLocal(remotePlaylist: Playlist, localPlaylist: Playlist?): Playlist {
        if (localPlaylist == null) {
            return remotePlaylist
        }
        val localMap = localPlaylist.songs.associateBy { it.youtubeId }
        val mergedSongs = remotePlaylist.songs.map { remoteSong ->
            localMap[remoteSong.youtubeId]?.copy(uid = UUID.randomUUID().toString()) ?: remoteSong
        }
        return remotePlaylist.copy(songs = mergedSongs)
    }
}
