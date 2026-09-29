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
private data class BetterLyricsResponse(
    val ttml: String? = null,
    val error: String? = null
)

class BetterLyricsProvider(
    private val json: Json = Json { ignoreUnknownKeys = true }
) : LyricsProvider {

    override val name = "BetterLyrics"

    override suspend fun getLyrics(query: LyricsQuery): Lyrics? =
        withContext(Dispatchers.IO) {
            val ttml = fetchTtml(query) ?: return@withContext null
            val lines = parseTtml(ttml)
            if (lines.isEmpty()) null else Lyrics(lines = lines, unsyncedLyrics = null)
        }

    private fun fetchTtml(query: LyricsQuery): String? =
        try {
            val request = Request.Builder()
                .header("User-Agent", USER_AGENT)
                .url(buildGetUrl(query))
                .build()

            UmihiHttpClient.client.newCall(request).execute().use { response ->
                when {
                    response.code == HTTP_UNAUTHORIZED -> null
                    !response.isSuccessful -> null
                    else -> response.body?.string()
                        ?.let { json.decodeFromString<BetterLyricsResponse>(it).ttml }
                        ?.takeIf { it.isNotBlank() }
                }
            }
        } catch (e: Exception) {
            LogHelper.printe(e.message.toString())
            null
        }

    private fun buildGetUrl(query: LyricsQuery): HttpUrl =
        GET_URL.newBuilder()
            .addQueryParameter(PARAM_SONG, query.title)
            .addQueryParameter(PARAM_ARTIST, query.artist)
            .addQueryParameter(PARAM_ALBUM, query.album)
            .addQueryParameter(PARAM_DURATION, (query.durationMs / MS_PER_SECOND).toString())
            .build()


    internal fun parseTtml(raw: String): List<SyncedLine> =
        PARAGRAPH_REGEX.findAll(raw)
            .mapNotNull { match ->
                val attributes = match.groupValues[1]
                val begin =
                    BEGIN_REGEX.find(attributes)?.groupValues?.get(1) ?: return@mapNotNull null
                val startMs = parseTtmlTime(begin) ?: return@mapNotNull null

                val text = SPAN_TEXT_REGEX.replace(match.groupValues[2]) { it.groupValues[1] }
                    .let { SPAN_TAG_REGEX.replace(it, "") }
                    .decodeEntities()
                    .replace(WHITESPACE_REGEX, " ")
                    .trim()

                if (text.isEmpty()) null else SyncedLine(timeMs = startMs, text = text)
            }
            .sortedBy { it.timeMs }
            .toList()

    internal fun parseTtmlTime(value: String): Long? {
        val parts = value.trim().split(':')
        if (parts.isEmpty() || parts.size > 3) {
            return null
        }

        var total = 0L
        for (index in parts.indices) {
            val isLast = index == parts.lastIndex
            val seconds = if (isLast) {
                parts[index].toDoubleOrNull() ?: return null
            } else {
                parts[index].toLongOrNull()?.toDouble() ?: return null
            }
            if (seconds < 0) {
                return null
            }

            val multiplier = when (parts.size - 1 - index) {
                2 -> MS_PER_HOUR
                1 -> MS_PER_MINUTE
                else -> MS_PER_SECOND
            }
            total += (seconds * multiplier).toLong()
        }
        return total
    }

    private fun String.decodeEntities(): String =
        ENTITY_REGEX.replace(this) { match ->
            when (val entity = match.groupValues[1]) {
                "amp" -> "&"
                "lt" -> "<"
                "gt" -> ">"
                "quot" -> "\""
                "apos" -> "'"
                else -> if (entity.startsWith("#")) {
                    val code = entity.removePrefix("#").let {
                        if (it.startsWith("x", ignoreCase = true)) {
                            it.drop(1).toIntOrNull(16)
                        } else {
                            it.toIntOrNull()
                        }
                    }
                    code?.toChar()?.toString() ?: match.value
                } else {
                    match.value
                }
            }
        }

    companion object {
        private const val USER_AGENT =
            "umihi-music v${BuildConfig.VERSION_NAME} (https://github.com/ilianoKokoro/umihi-music)"
        private val GET_URL = "https://api.betterlyrics.org/getLyrics".toHttpUrl()
        private const val PARAM_SONG = "s"
        private const val PARAM_ARTIST = "a"
        private const val PARAM_ALBUM = "al"
        private const val PARAM_DURATION = "d"
        private const val MS_PER_SECOND = 1_000
        private const val MS_PER_MINUTE = 60_000
        private const val MS_PER_HOUR = 3_600_000
        private const val HTTP_UNAUTHORIZED = 401

        private val PARAGRAPH_REGEX =
            Regex("""<p\b([^>]*)>(.*?)</p>""", RegexOption.DOT_MATCHES_ALL)
        private val BEGIN_REGEX = Regex("""\bbegin="([^"]*)"""")
        private val SPAN_TEXT_REGEX =
            Regex("""<span\b[^>]*>(.*?)</span>""", RegexOption.DOT_MATCHES_ALL)
        private val SPAN_TAG_REGEX = Regex("""<[^>]*>""")
        private val WHITESPACE_REGEX = Regex("""\s+""")
        private val ENTITY_REGEX = Regex("""&(#\d+|#[xX][0-9a-fA-F]+|[a-zA-Z]+);""")
    }
}
