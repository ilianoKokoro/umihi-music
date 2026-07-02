package ca.ilianokokoro.umihi.music.core.youtube

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.UmihiHttpClient
import ca.ilianokokoro.umihi.music.core.helpers.LogHelper
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.formatDecimal
import ca.ilianokokoro.umihi.music.core.managers.PlayerManager
import ca.ilianokokoro.umihi.music.models.UmihiSettings
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

object YoutubeStatsTracker {
    // --- Attributes ---

    private const val CPN_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_"
    private const val CPN_LENGTH = 16

    private data class PlaybackTrackingUrls(
        val playbackUrl: String?,
        val watchtimeUrl: String?,
    )

    private data class PlaybackTrackingState(
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
            LogHelper.printd("Playback tracking skipped: sendPlaybackData=${settings.sendPlaybackData}, cookies=${!settings.cookies.isEmpty()}")
            return
        }

        scope.launch {
            try {
                val playerResponse = YoutubeApiClient.getPlayerInfo(
                    videoId = videoId, visitorData = visitorData, settings = settings,
                )
                val trackingUrls = extractTrackingUrls(playerResponse)
                val playbackUrl = trackingUrls.playbackUrl
                val watchtimeUrl = trackingUrls.watchtimeUrl

                if (playbackUrl == null || watchtimeUrl == null) {
                    LogHelper.printe("No tracking URLs in player response for $videoId")
                    return@launch
                }

                val referrer = "${Constants.YoutubeApi.ORIGIN}/watch?v=$videoId"

                startPlaybackTracking(
                    videoId = videoId, playbackUrl = playbackUrl, watchtimeUrl = watchtimeUrl,
                    settings = settings, playlistId = null, referrer = referrer,
                )
            } catch (e: Exception) {
                LogHelper.printe("Failed to start playback tracking: ${e.message}", exception = e)
            }
        }
    }


    private fun generateCpn(): String = buildString {
        repeat(CPN_LENGTH) { append(CPN_CHARS[Random.nextInt(CPN_CHARS.length)]) }
    }

    private fun startPlaybackTracking(
        videoId: String,
        playbackUrl: String,
        watchtimeUrl: String,
        settings: UmihiSettings,
        playlistId: String?,
        referrer: String?,
    ) {
        stopPlaybackTracking()

        if (!settings.sendPlaybackData || settings.cookies.isEmpty()) {
            return
        }

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
            LogHelper.printd("Playback tracking started for $videoId (cpn=$cpn)")
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
                        LogHelper.printd("Watchtime update: $st → $et ($videoId)")

                        lastReportedPosition = posSec
                    } else {
                        sendWatchtimeComplete(
                            baseUrl = watchtimeUrl, cpn = cpn, durationSeconds = durSec,
                            settings = settings, playlistId = playlistId, referrer = referrer,
                        )
                        LogHelper.printd("Watchtime complete: $durSec ($videoId)")

                        stopPlaybackTracking()
                    }
                }
            }
        }
    }

    private fun extractTrackingUrls(jsonString: String): PlaybackTrackingUrls {
        return try {
            val root = JSONObject(jsonString)
            val tracking =
                root.optJSONObject("playbackTracking") ?: return PlaybackTrackingUrls(null, null)

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

    private suspend fun sendInitPlayback(
        baseUrl: String,
        cpn: String,
        settings: UmihiSettings,
        playlistId: String? = null,
        referrer: String? = null,
    ): Int? = withContext(Dispatchers.IO) {
        if (!settings.canTrack) {
            return@withContext null
        }

        val urlBuilder = buildUrl(baseUrl, cpn, playlistId, referrer)
        val request = Request.Builder()
            .url(urlBuilder.build())
            .post(FormBody.Builder().build())
            .applyStatsHeaders(settings)
            .build()

        executeRequest(request)
    }

    private suspend fun sendWatchtimeUpdate(
        baseUrl: String,
        cpn: String,
        st: String,
        et: String,
        settings: UmihiSettings,
        playlistId: String? = null,
        referrer: String? = null,
    ): Int? = withContext(Dispatchers.IO) {
        if (!settings.canTrack) {
            return@withContext null
        }

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

    private suspend fun sendWatchtimeComplete(
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

    private fun Request.Builder.applyStatsHeaders(settings: UmihiSettings): Request.Builder {
        val nowMs = System.currentTimeMillis()
        val utcOffsetMinutes = (TimeZone.getDefault()?.rawOffset ?: 0) / 60000

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
                LogHelper.printd(
                    "PlaybackStats: ${request.url.encodedPath}?${
                        (request.url.encodedQuery ?: "").take(
                            80
                        )
                    }... -> ${response.code}"
                )
                response.code
            }
        } catch (e: Exception) {
            LogHelper.printe("PlaybackStats request failed: ${e.message}", exception = e)
            null
        }
    }
}