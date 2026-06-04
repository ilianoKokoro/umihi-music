package ca.ilianokokoro.umihi.music.data.repositories

import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.data.datasources.PlaylistDataSource
import ca.ilianokokoro.umihi.music.extensions.toException
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.Privacy
import ca.ilianokokoro.umihi.music.models.UmihiSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class PlaylistRepository {
    private val playlistDataSource = PlaylistDataSource()
    fun retrieveAll(settings: UmihiSettings): Flow<ApiResult<List<PlaylistInfo>>> {
        return flow {
            emit(ApiResult.Loading)
            emit(ApiResult.Success(playlistDataSource.retrieveAll(settings)))
        }.catch { e ->
            emit(ApiResult.Error(e.toException()))
        }.flowOn(Dispatchers.IO)
    }

    fun retrieveOne(
        playlist: Playlist,
        settings: UmihiSettings
    ): Flow<ApiResult<Playlist>> {
        return flow {
            emit(ApiResult.Loading)
            emit(ApiResult.Success(playlistDataSource.retrieveOne(playlist, settings)))
        }.catch { e ->
            emit(ApiResult.Error(e.toException()))
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
                    playlistDataSource.create(
                        title,
                        description,
                        privacy,
                        settings
                    )
                )
            )
        }.catch { e ->
            emit(ApiResult.Error(e.toException()))
        }.flowOn(Dispatchers.IO)
    }

    fun delete(
        playlist: PlaylistInfo,
        settings: UmihiSettings
    ): Flow<ApiResult<Unit>> {
        return flow {
            emit(ApiResult.Loading)
            emit(
                ApiResult.Success(
                    playlistDataSource.delete(
                        playlist,
                        settings
                    )
                )
            )
        }.catch { e ->
            emit(ApiResult.Error(e.toException()))
        }.flowOn(Dispatchers.IO)
    }
}