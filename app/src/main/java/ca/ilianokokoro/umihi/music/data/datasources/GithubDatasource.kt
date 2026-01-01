package ca.ilianokokoro.umihi.music.data.datasources

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.models.dto.GithubCommitResponse
import ca.ilianokokoro.umihi.music.models.dto.GithubReleaseResponse
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import kotlinx.serialization.json.Json

class GithubDatasource {
    private val json = Json { ignoreUnknownKeys = true }

    fun getLatestRelease(): GithubReleaseResponse {
        val (_, _, result) = Constants.Url.GITHUB_RELEASE_API.httpGet()
            .responseJson()


        return when (result) {
            is Result.Success -> {
                json.decodeFromString<GithubReleaseResponse>(result.value.content)
            }


            is Result.Failure -> {
                throw Exception("Failed to get the latest GitHub release version name")
            }
        }
    }

    fun getLatestCommit(): String {
        val (_, _, result) = Constants.Url.GITHUB_COMMIT_API.httpGet()
            .responseJson()


        return when (result) {
            is Result.Success -> {
                json.decodeFromString<GithubCommitResponse>(result.value.content).sha
            }


            is Result.Failure -> {
                throw Exception("Failed to get the latest commit")
            }
        }
    }
}