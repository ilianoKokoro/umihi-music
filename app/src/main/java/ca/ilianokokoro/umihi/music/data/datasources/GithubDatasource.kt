package ca.ilianokokoro.umihi.music.data.datasources

import ca.ilianokokoro.umihi.music.core.UmihiHttpClient
import ca.ilianokokoro.umihi.music.core.exceptions.GithubRateLimitException
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
        val body = get(releaseUrl)
        return json.decodeFromString<GithubReleaseResponse>(body)
    }

    private suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        UmihiHttpClient.client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val retryAfter = response.header("Retry-After")
                var seconds = retryAfter?.toLongOrNull()

                if (seconds == null) {
                    val remaining = response.header("X-RateLimit-Remaining")
                    val resetEpoch = response.header("X-RateLimit-Reset")
                    if (remaining == "0" && resetEpoch != null) {
                        val now = System.currentTimeMillis() / 1000
                        val resetTime = resetEpoch.toLongOrNull()
                        if (resetTime != null && resetTime > now) {
                            seconds = resetTime - now
                        }
                    }
                }

                seconds?.let {
                    throw GithubRateLimitException(
                        retryAfterSeconds = it,
                        "Github rate limited, retry in $it seconds"
                    )
                }

                throw IOException("HTTP ${response.code}: ${response.message}")
            }

            response.body?.string()
                ?: throw IOException("GitHub Empty response body")
        }
    }
}
