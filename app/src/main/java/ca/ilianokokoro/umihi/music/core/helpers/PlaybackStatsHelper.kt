package ca.ilianokokoro.umihi.music.core.helpers

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.UmihiHttpClient
import ca.ilianokokoro.umihi.music.models.UmihiSettings
import ca.ilianokokoro.umihi.music.core.helpers.YoutubeRequestHelper
import ca.ilianokokoro.umihi.music.core.managers.PlayerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import org.json.JSONObject
import java.util.TimeZone
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.formatDecimal
import ca.ilianokokoro.umihi.music.core.helpers.visitorData

object PlaybackStatsHelper {
    private const val CPN_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_"
    private const val CPN_LENGTH = 16

    /**
     * Generates a Client Playback Nonce — a 16-character random string
     * using YouTube's CPN character set.
     */
    fun generateCpn(): String = buildString {
        repeat(CPN_LENGTH) { append(CPN_CHARS[Random.nextInt(CPN_CHARS.length)]) }
    }

    /**
     * Sends the initial playback tracking request (POST with form body).
     * Registers that playback has started for a video.
     *
     * The base URL comes from the player response's
     * `playbackTracking.videostatsPlaybackUrl.baseUrl`.
     */
    suspend fun sendInitPlayback(
        baseUrl: String,
        cpn: String,
        settings: UmihiSettings,
        playlistId: String? = null,
        referrer: String? = null,
    ): Int? = withContext(Dispatchers.IO) {
        if (!canTrack(settings)) return@withContext null

        val urlBuilder = buildUrl(baseUrl, cpn, playlistId, referrer)
        val request = Request.Builder()
            .url(urlBuilder.build())
            .post(FormBody.Builder().build())
            .applyStatsHeaders(settings)
            .build()

        executeRequest(request)
    }

    /**
     * Sends a watchtime update request (POST with form body).
     * Called periodically during playback to report progress.
     *
     * @param st  Start time(s) in seconds — comma-separated for batched reports
     * @param et  End time(s) in seconds — comma-separated for batched reports
     */
    suspend fun sendWatchtimeUpdate(
        baseUrl: String,
        cpn: String,
        st: String,
        et: String,
        settings: UmihiSettings,
        playlistId: String? = null,
        referrer: String? = null,
    ): Int? = withContext(Dispatchers.IO) {
        if (!canTrack(settings)) return@withContext null

        val urlBuilder = buildUrl(baseUrl, cpn, playlistId, referrer)
        urlBuilder.addEncodedQueryParameter("st", st)
        urlBuilder.addEncodedQueryParameter("et", et)

        val request = Request.Builder()
            .url(urlBuilder.build())
            .post(FormBody.Builder().build())
            .applyStatsHeaders(settings)
            .build()

        executeRequest(request)
    }

    /**
     * Sends the final watchtime update marking playback complete.
     * Both st and et are set to the full duration.
     */
    suspend fun sendWatchtimeComplete(
        baseUrl: String,
        cpn: String,
        durationSeconds: Float,
        settings: UmihiSettings,
        playlistId: String? = null,
        referrer: String? = null,
    ): Int? {
        val durationStr = durationSeconds.formatDecimal()
        return sendWatchtimeUpdate(
            baseUrl = baseUrl,
            cpn = cpn,
            st = durationStr,
            et = durationStr,
            settings = settings,
            playlistId = playlistId,
            referrer = referrer,
        )
    }

    // --- Internal ---

    @Suppress("MemberVisibilityCanBePrivate")
    data class PlaybackTrackingUrls(
        val playbackUrl: String?,
        val watchtimeUrl: String?,
    )

    data class PlaybackTrackingState(
        val videoId: String,
        val cpn: String,
        val watchtimeUrl: String,
        val settings: UmihiSettings,
        val playlistId: String? = null,
        val referrer: String? = null,
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var trackingState: PlaybackTrackingState? = null

    @Volatile
    private var trackingJob: Job? = null

    fun stopPlaybackTracking() {
        trackingJob?.cancel()
        trackingJob = null
        trackingState = null
    }

    fun onPlaybackStarted(
        videoId: String,
        settings: UmihiSettings,
    ) {
        if (!settings.sendPlaybackData || settings.cookies.isEmpty()) {
            stopPlaybackTracking()
            UmihiHelper.printd("Playback tracking skipped: sendPlaybackData=${settings.sendPlaybackData}, cookies=${!settings.cookies.isEmpty()}")
            return
        }

        scope.launch {
            try {
                val playerResponse = YoutubeRequestHelper.getPlayerInfo(
                    videoId = videoId, visitorData = visitorData, settings = settings,
                )
                val trackingUrls = extractTrackingUrls(playerResponse)
                val playbackUrl = trackingUrls.playbackUrl
                val watchtimeUrl = trackingUrls.watchtimeUrl

                if (playbackUrl == null || watchtimeUrl == null) {
                    UmihiHelper.printe("No tracking URLs in player response for $videoId")
                    return@launch
                }

                val referrer = "${Constants.YoutubeApi.ORIGIN}/watch?v=$videoId"

                startPlaybackTracking(
                    videoId = videoId, playbackUrl = playbackUrl, watchtimeUrl = watchtimeUrl,
                    settings = settings, playlistId = null, referrer = referrer,
                )
            } catch (e: Exception) {
                UmihiHelper.printe("Failed to start playback tracking: ${e.message}", exception = e)
            }
        }
    }

    fun startPlaybackTracking(
        videoId: String,
        playbackUrl: String,
        watchtimeUrl: String,
        settings: UmihiSettings,
        playlistId: String?,
        referrer: String?,
    ) {
        stopPlaybackTracking()

        if (!settings.sendPlaybackData || settings.cookies.isEmpty()) return

        val cpn = generateCpn()
        trackingState = PlaybackTrackingState(
            videoId = videoId, cpn = cpn, watchtimeUrl = watchtimeUrl,
            settings = settings, playlistId = playlistId, referrer = referrer,
        )

        scope.launch {
            sendInitPlayback(
                baseUrl = playbackUrl, cpn = cpn, settings = settings,
                playlistId = playlistId, referrer = referrer,
            )
            UmihiHelper.printd("Playback tracking started for $videoId (cpn=$cpn)")
        }

        trackingJob = scope.launch {
            var lastReportedPosition = 0f

            while (isActive) {
                delay(Constants.Player.Tracking.WATCHTIME_INTERVAL_MS.milliseconds)

                val (posSec, durSec) = PlayerManager.getPlaybackPosition() ?: continue

                if (posSec >= lastReportedPosition + Constants.Player.Tracking.POSITION_TOLERANCE_SEC) {
                    val nextCheckpoint = posSec + Constants.Player.Tracking.WATCHTIME_ADVANCE_SEC

                    if (nextCheckpoint < durSec) {
                        val st = lastReportedPosition.formatDecimal()
                        val et = posSec.formatDecimal()

                        sendWatchtimeUpdate(
                            baseUrl = watchtimeUrl, cpn = cpn, st = st, et = et,
                            settings = settings, playlistId = playlistId, referrer = referrer,
                        )
                        UmihiHelper.printd("Watchtime update: $st → $et ($videoId)")

                        lastReportedPosition = posSec
                    } else {
                        sendWatchtimeComplete(
                            baseUrl = watchtimeUrl, cpn = cpn, durationSeconds = durSec,
                            settings = settings, playlistId = playlistId, referrer = referrer,
                        )
                        UmihiHelper.printd("Watchtime complete: $durSec ($videoId)")

                        stopPlaybackTracking()
                    }
                }
            }
        }
    }

    /**
     * Extracts the `playbackTracking` URLs from a player response JSON string.
     *
     * Expected structure from /youtubei/v1/player:
     * ```json
     * {
     *   "playbackTracking": {
     *     "videostatsPlaybackUrl": { "baseUrl": "https://..." },
     *     "videostatsWatchtimeUrl": { "baseUrl": "https://..." }
     *   }
     * }
     * ```
     */
    fun extractTrackingUrls(jsonString: String): PlaybackTrackingUrls {
        return try {
            val root = JSONObject(jsonString)
            val tracking = root.optJSONObject("playbackTracking") ?: return PlaybackTrackingUrls(null, null)

            fun extractBaseUrl(key: String): String? {
                val obj = tracking.optJSONObject(key) ?: return null
                val url = obj.optString("baseUrl", "")
                return url.ifBlank { null }
            }

            PlaybackTrackingUrls(
                playbackUrl = extractBaseUrl("videostatsPlaybackUrl"),
                watchtimeUrl = extractBaseUrl("videostatsWatchtimeUrl"),
            )
        } catch (_: Exception) {
            PlaybackTrackingUrls(null, null)
        }
    }

    private fun canTrack(settings: UmihiSettings): Boolean {
        return settings.sendPlaybackData && !settings.cookies.isEmpty()
    }

    private fun buildUrl(
        baseUrl: String,
        cpn: String,
        playlistId: String?,
        referrer: String?,
    ): HttpUrl.Builder {
        val builder = checkNotNull(baseUrl.toHttpUrl().newBuilder()) {
            "Invalid playback tracking URL: $baseUrl"
        }

        builder.addEncodedQueryParameter("cpn", cpn)
        builder.addEncodedQueryParameter("ver", "2")
        builder.addEncodedQueryParameter("c", "WEB_REMIX")

        if (playlistId != null) {
            builder.addEncodedQueryParameter("list", playlistId)
        }

        if (referrer != null) {
            builder.addEncodedQueryParameter("referrer", referrer)
        }

        return builder
    }

    /**
     * Applies headers matching YouTube Music's stats endpoint expectations.
     * Based on observed browser traffic to /api/stats/playback.
     */
    private fun Request.Builder.applyStatsHeaders(settings: UmihiSettings): Request.Builder {
        val nowMs = System.currentTimeMillis()
        val utcOffsetMinutes = (TimeZone.getDefault()?.rawOffset ?: 0) / 60000

        // Auth / cookie headers from YoutubeAuthHelper (SAPISID hash, Cookie, etc.)
        val authHeaders = YoutubeAuthHelper.getHeaders(settings.cookies)
        authHeaders.forEach { (name, value) ->
            addHeader(name, value)
        }

        removeHeader("X-Goog-Api-Format-Version")
        header("Content-Type", "application/x-www-form-urlencoded")

        header("X-Goog-Event-Time", nowMs.toString())
        header("X-Goog-Request-Time", nowMs.toString())

        settings.dataSyncId?.let {
            header("X-YouTube-DataSync-Id", "$it||")
        }
        header("X-Goog-AuthUser", "1")

        visitorData?.let {
            header("X-Goog-Visitor-Id", it)
        }

        header("X-YouTube-Utc-Offset", utcOffsetMinutes.toString())
        header("X-YouTube-Time-Zone", TimeZone.getDefault()?.id ?: "UTC")
        header("Origin", Constants.YoutubeApi.ORIGIN)

        return this
    }

    private suspend fun executeRequest(request: Request): Int? {
        return try {
            UmihiHttpClient.client.newCall(request).execute().use { response ->
                UmihiHelper.printd(
                    "PlaybackStats: ${request.url.encodedPath}?${(request.url.encodedQuery ?: "").take(80)}... -> ${response.code}"
                )
                response.code
            }
        } catch (e: Exception) {
            UmihiHelper.printe("PlaybackStats request failed: ${e.message}", exception = e)
            null
        }
    }

}
