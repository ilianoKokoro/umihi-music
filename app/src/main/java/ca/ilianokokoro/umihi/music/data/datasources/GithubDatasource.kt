package ca.ilianokokoro.umihi.music.data.datasources

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.UmihiHttpClient
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper
import ca.ilianokokoro.umihi.music.models.dto.GithubCommitResponse
import ca.ilianokokoro.umihi.music.models.dto.GithubReleaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Request
import java.io.IOException

class GithubDatasource {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getLatestRelease(): GithubReleaseResponse {
        return try {
            val body = get(Constants.Url.Github.Release.API)
            json.decodeFromString<GithubReleaseResponse>(body)
        } catch (e: Exception) {
            UmihiHelper.printe(e.toString())
            throw Exception("Failed to get the latest GitHub release version name", e)
        }
    }

    suspend fun getLatestCommit(): GithubCommitResponse {
        return try {
            val body = get(Constants.Url.Github.Beta.API)
            json.decodeFromString<GithubCommitResponse>(body)
        } catch (e: Exception) {
            UmihiHelper.printe(e.toString())
            throw Exception("Failed to get the latest commit", e)
        }
    }

    private suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        UmihiHttpClient.client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}: ${response.message}")
            }

            response.body?.string()
                ?: throw IOException("Empty response body")
        }
    }
}