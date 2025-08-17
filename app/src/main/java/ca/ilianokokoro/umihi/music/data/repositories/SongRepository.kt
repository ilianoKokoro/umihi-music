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

    fun getStreamUrlFromId(song: Song): Flow<ApiResult<String>> {
        return flow {
            try {
                emit(ApiResult.Loading)
                emit(ApiResult.Success(songDataSource.getStreamUrlFromId(song)))
            } catch (e: Exception) {
                emit(ApiResult.Error(e))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getSongThumbnail(id: String): Flow<ApiResult<String>> {
        return flow {
            try {
                emit(ApiResult.Loading)
                emit(ApiResult.Success(songDataSource.getSongThumbnail(id)))
            } catch (e: Exception) {
                emit(ApiResult.Error(e))
            }
        }.flowOn(Dispatchers.IO)
    }
}