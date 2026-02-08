package ca.ilianokokoro.umihi.music.data.repositories

import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.data.datasources.SongDataSource
import ca.ilianokokoro.umihi.music.models.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SongRepository {
    private val songDataSource = SongDataSource()

    fun search(query: String): Flow<ApiResult<List<Song>>> {
        return flow {
            try {
                emit(ApiResult.Loading)
                emit(ApiResult.Success(songDataSource.search(query)))
            } catch (e: Exception) {
                emit(ApiResult.Error(e))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getSongInfo(songId: String): Flow<ApiResult<Song>> {
        return flow {
            try {
                emit(ApiResult.Loading)
                emit(ApiResult.Success(songDataSource.getSongInfo(songId)))
            } catch (e: Exception) {
                emit(ApiResult.Error(e))
            }
        }.flowOn(Dispatchers.IO)
    }
}