package ca.ilianokokoro.umihi.music.data.datasources

import ca.ilianokokoro.umihi.music.core.UmihiHttpClient
import ca.ilianokokoro.umihi.music.core.helpers.LogHelper
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

    suspend fun getReleaseInfoByUrl(releaseUrl: String): GithubReleaseResponse {
        return try {
            val body = get(releaseUrl)
            json.decodeFromString<GithubReleaseResponse>(body)
        } catch (e: Exception) {
            LogHelper.printe(e.toString())
            throw Exception("Failed to get the GitHub release infor for $releaseUrl", e)
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