package ca.ilianokokoro.umihi.music.data.repositories

import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.data.datasources.SongDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SongRepository {
    private val songDataSource = SongDataSource()

    fun getStreamUrlFromId(songId: String): Flow<ApiResult<String>> {
        return flow {
            try {
                emit(ApiResult.Loading)
                emit(ApiResult.Success(songDataSource.getStreamUrlFromId(songId)))
            } catch (e: Exception) {
                emit(ApiResult.Error(e))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getSongThumbnail(songId: String): Flow<ApiResult<String>> {
        return flow {
            try {
                emit(ApiResult.Loading)
                emit(ApiResult.Success(songDataSource.getSongThumbnail(songId)))
            } catch (e: Exception) {
                emit(ApiResult.Error(e))
            }
        }.flowOn(Dispatchers.IO)
    }
}