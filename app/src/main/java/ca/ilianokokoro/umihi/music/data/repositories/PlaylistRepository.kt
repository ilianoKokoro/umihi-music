package ca.ilianokokoro.umihi.music.data.repositories

import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.data.datasources.PlaylistDataSource
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.UmihiSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class PlaylistRepository {
    private val playlistDataSource = PlaylistDataSource()
    fun retrieveAll(settings: UmihiSettings): Flow<ApiResult<List<PlaylistInfo>>> {
        return flow {
            try {
                emit(ApiResult.Loading)
                emit(ApiResult.Success(playlistDataSource.retrieveAll(settings)))
            } catch (e: Exception) {
                emit(ApiResult.Error(e))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun retrieveOne(
        playlist: Playlist,
        settings: UmihiSettings
    ): Flow<ApiResult<Playlist>> {
        return flow {
            try {
                emit(ApiResult.Loading)
                emit(ApiResult.Success(playlistDataSource.retrieveOne(playlist, settings)))
            } catch (e: Exception) {
                emit(ApiResult.Error(e))
            }
        }.flowOn(Dispatchers.IO)
    }
}