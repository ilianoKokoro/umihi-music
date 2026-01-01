package ca.ilianokokoro.umihi.music.data.repositories


import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.data.datasources.GithubDatasource
import ca.ilianokokoro.umihi.music.models.dto.GithubReleaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class GithubRepository() {
    private val githubRepository = GithubDatasource()
    fun getLatestVersionName(): Flow<ApiResult<GithubReleaseResponse>> {
        return flow {
            try {
                emit(ApiResult.Loading)
                emit(ApiResult.Success(githubRepository.getLatestRelease()))
            } catch (ex: Exception) {
                emit(ApiResult.Error(ex))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getLatestCommit(): Flow<ApiResult<String>> {
        return flow {
            try {
                emit(ApiResult.Loading)
                emit(ApiResult.Success(githubRepository.getLatestCommit()))
            } catch (ex: Exception) {
                emit(ApiResult.Error(ex))
            }
        }.flowOn(Dispatchers.IO)
    }
}