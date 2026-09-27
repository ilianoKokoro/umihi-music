package ca.ilianokokoro.umihi.music.models.lyrics.providers

import ca.ilianokokoro.umihi.music.BuildConfig
import ca.ilianokokoro.umihi.music.core.UmihiHttpClient
import ca.ilianokokoro.umihi.music.core.helpers.LogHelper
import ca.ilianokokoro.umihi.music.models.lyrics.Lyrics
import ca.ilianokokoro.umihi.music.models.lyrics.LyricsProvider
import ca.ilianokokoro.umihi.music.models.lyrics.LyricsQuery
import ca.ilianokokoro.umihi.music.models.lyrics.SyncedLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request

@Serializable
private data class LrcLibResponse(
    val trackName: String,
    val artistName: String,
    val albumName: String? = null,
    val duration: Double? = null,
    val instrumental: Boolean = false,
    val plainLyrics: String? = null,
    val syncedLyrics: String? = null
)

class LrcLibProvider(
    private val json: Json = Json { ignoreUnknownKeys = true }
) : LyricsProvider {

    override val name = "LrcLib"

    override suspend fun getLyrics(query: LyricsQuery): Lyrics? =
        withContext(Dispatchers.IO) {
            fetchExact(query) ?: fetchSearch(query)
        }

    private fun fetchExact(query: LyricsQuery): Lyrics? =
        try {
            val request = Request.Builder()
                .header("User-Agent", USER_AGENT)
                .url(buildGetUrl(query)).build()
            UmihiHttpClient.client.newCall(request).execute().use { response ->
                when {
                    response.code == HTTP_NOT_FOUND -> null
                    !response.isSuccessful -> null
                    else -> {
                        response.body?.string()
                            ?.let { json.decodeFromString<LrcLibResponse>(it).toLyrics() }
                    }
                }
            }
        } catch (e: Exception) {
            LogHelper.printe(e.message.toString())
            null
        }

    private fun fetchSearch(query: LyricsQuery): Lyrics? =
        try {
            val request = Request.Builder()
                .header("User-Agent", USER_AGENT)
                .url(buildSearchUrl(query)).build()
            UmihiHttpClient.client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@use null
                }
                val body = response.body?.string() ?: return@use null
                json.decodeFromString<List<LrcLibResponse>>(body)
                    .firstNotNullOfOrNull { it.toLyrics() }
            }
        } catch (e: Exception) {
            LogHelper.printe(e.message.toString())
            null
        }

    private fun buildGetUrl(query: LyricsQuery): HttpUrl =
        GET_URL.newBuilder()
            .addQueryParameter(PARAM_TRACK, query.title)
            .addQueryParameter(PARAM_ARTIST, query.artist)
            .addQueryParameter(PARAM_DURATION, (query.durationMs / MS_PER_SECOND).toString())
            .build()

    private fun buildSearchUrl(query: LyricsQuery): HttpUrl =
        SEARCH_URL.newBuilder()
            .addQueryParameter(PARAM_TRACK, query.title)
            .addQueryParameter(PARAM_ARTIST, query.artist)
            .build()

    private fun LrcLibResponse.toLyrics(): Lyrics? {
        if (this.syncedLyrics.isNullOrBlank()) {
            return null
        }

        return Lyrics(lines = parseSyncedLyrics(this.syncedLyrics))
    }

    private fun parseSyncedLyrics(raw: String): List<SyncedLine> {
        return raw.lineSequence()
            .mapNotNull { LRC_LINE_REGEX.matchEntire(it.trim()) }
            .map { match ->
                val (min, sec, frac, text) = match.destructured
                val fracMs = if (frac.length == 2) frac.toLong() * 10 else frac.toLong()
                SyncedLine(
                    timeMs = min.toLong() * 60_000 + sec.toLong() * 1_000 + fracMs,
                    text = text.trim()
                )
            }
            .toList()
    }

    companion object {
        private const val USER_AGENT =
            "umihi-music v${BuildConfig.VERSION_NAME} (https://github.com/ilianoKokoro/umihi-music)"
        private val GET_URL = "https://lrclib.net/api/get".toHttpUrl()
        private val SEARCH_URL = "https://lrclib.net/api/search".toHttpUrl()
        private const val PARAM_TRACK = "track_name"
        private const val PARAM_ARTIST = "artist_name"
        private const val PARAM_DURATION = "duration"
        private const val MS_PER_SECOND = 1_000
        private const val HTTP_NOT_FOUND = 404
        private val LRC_LINE_REGEX = Regex("""^\[(\d{2}):(\d{2})\.(\d{2,3})](.*)$""")
    }
}