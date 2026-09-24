package ca.ilianokokoro.umihi.music.data.repositories

import android.app.Application
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.data.datasources.SongDataSource
import ca.ilianokokoro.umihi.music.extensions.toException
import ca.ilianokokoro.umihi.music.models.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SongRepository(
    private val application: Application? = null
) {
    private val songDataSource = SongDataSource()
    private val datastoreRepository = application?.let { DatastoreRepository(it) }
    private val localSongDataSource = application?.let { AppDatabase.getInstance(it).songRepository() }

    fun search(query: String): Flow<ApiResult<List<Song>>> {
        return flow {
            emit(ApiResult.Loading)
            val offlineMode = datastoreRepository?.getSettings()?.offlineMode == true
            if (offlineMode) {
                val localSongs = localSongDataSource?.searchDownloaded(query) ?: emptyList()
                emit(ApiResult.Success(localSongs))
            } else {
                emit(ApiResult.Success(songDataSource.search(query)))
            }
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