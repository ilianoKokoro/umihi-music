package ca.ilianokokoro.umihi.music.data.repositories

import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.data.datasources.SongDataSource
import ca.ilianokokoro.umihi.music.extensions.toException
import ca.ilianokokoro.umihi.music.models.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SongRepository {
    private val songDataSource = SongDataSource()

    fun search(query: String): Flow<ApiResult<List<Song>>> {
        return flow {
            emit(ApiResult.Loading)
            emit(ApiResult.Success(songDataSource.search(query)))
        }.catch { e ->
            emit(ApiResult.Error(e.toException()))
        }.flowOn(Dispatchers.IO)
    }

    fun getSongInfo(songId: String): Flow<ApiResult<Song>> {
        return flow {
            emit(ApiResult.Loading)
            emit(ApiResult.Success(songDataSource.getSongInfo(songId)))
        }.catch { e ->
            emit(ApiResult.Error(e.toException()))
        }.flowOn(Dispatchers.IO)
    }
}