package ca.ilianokokoro.umihi.music.data.repositories

import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.data.datasources.PlaylistDataSource
import ca.ilianokokoro.umihi.music.models.Cookies
import ca.ilianokokoro.umihi.music.models.Playlist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class PlaylistRepository {
    private val playlistDataSource = PlaylistDataSource()
    fun retrieveAll(cookies: Cookies): Flow<ApiResult<List<Playlist>>> {
        return flow {
            try {
                emit(ApiResult.Loading)
                emit(ApiResult.Success(playlistDataSource.retrieveAll(cookies)))
            } catch (e: Exception) {
                emit(ApiResult.Error(e))
            }
        }.flowOn(Dispatchers.IO)
    }


}