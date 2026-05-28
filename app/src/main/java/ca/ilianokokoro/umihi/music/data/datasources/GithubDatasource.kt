package ca.ilianokokoro.umihi.music.data.datasources

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.UmihiHttpClient
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper
import ca.ilianokokoro.umihi.music.extensions.body
import ca.ilianokokoro.umihi.music.models.dto.GithubCommitResponse
import ca.ilianokokoro.umihi.music.models.dto.GithubReleaseResponse
import kotlinx.serialization.json.Json

class GithubDatasource {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getLatestRelease(): GithubReleaseResponse {
        try {
            val result = UmihiHttpClient.fuelClient.get(request = {
                this.url = Constants.Url.Github.Release.API
            })
            return json.decodeFromString<GithubReleaseResponse>(result.body)

        } catch (e: Exception) {
            UmihiHelper.printe(e.toString())
            throw Exception("Failed to get the latest GitHub release version name")
        }
    }

    suspend fun getLatestCommit(): GithubCommitResponse {
        try {
            val result = UmihiHttpClient.fuelClient.get(request = {
                this.url = Constants.Url.Github.Beta.API
            })
            return json.decodeFromString<GithubCommitResponse>(result.body)

        } catch (e: Exception) {
            UmihiHelper.printe(e.toString())
            throw Exception("Failed to get the latest commit")
        }
    }
}