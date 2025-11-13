package ca.ilianokokoro.umihi.music.data.repositories


import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.data.datasources.GithubDatasource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class GithubRepository() {
    private val githubRepository = GithubDatasource()
    fun getLatestVersionName(): Flow<ApiResult<String>> {
        return flow {
            try {
                emit(ApiResult.Loading)
                emit(ApiResult.Success(githubRepository.getLatestVersionName()))
            } catch (ex: Exception) {
                emit(ApiResult.Error(ex))
            }
        }.flowOn(Dispatchers.IO)
    }
}