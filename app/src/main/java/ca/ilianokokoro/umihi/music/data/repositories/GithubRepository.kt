package ca.ilianokokoro.umihi.music.data.repositories


import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.data.datasources.GithubDatasource
import ca.ilianokokoro.umihi.music.extensions.toException
import ca.ilianokokoro.umihi.music.models.dto.GithubCommitResponse
import ca.ilianokokoro.umihi.music.models.dto.GithubReleaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class GithubRepository {
    private val githubRepository = GithubDatasource()
    fun getLatestVersionName(): Flow<ApiResult<GithubReleaseResponse>> {
        return flow {
            emit(ApiResult.Loading)
            emit(ApiResult.Success(githubRepository.getLatestRelease()))
        }.catch { e ->
            emit(ApiResult.Error(e.toException()))
        }.flowOn(Dispatchers.IO)
    }

    fun getLatestCommit(): Flow<ApiResult<GithubCommitResponse>> {
        return flow {
            emit(ApiResult.Loading)
            emit(ApiResult.Success(githubRepository.getLatestCommit()))
        }.catch { e ->
            emit(ApiResult.Error(e.toException()))
        }.flowOn(Dispatchers.IO)
    }
}