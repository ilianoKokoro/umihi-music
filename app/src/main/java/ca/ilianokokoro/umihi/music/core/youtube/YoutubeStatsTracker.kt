package ca.ilianokokoro.umihi.music.core.youtube

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.UmihiHttpClient
import ca.ilianokokoro.umihi.music.core.helpers.LogHelper
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.formatDecimal
import ca.ilianokokoro.umihi.music.core.managers.PlayerManager
import ca.ilianokokoro.umihi.music.core.youtube.YoutubeAuthHelper.applyHeaders
import ca.ilianokokoro.umihi.music.extensions.getClientName
import ca.ilianokokoro.umihi.music.models.UmihiSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
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
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import org.json.JSONObject
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.milliseconds

object YoutubeStatsTracker {

    private data class PlaybackTrackingUrls(
        val playbackUrl: String?,
        val watchtimeUrl: String?,
        val clientName: String,
    )

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        LogHelper.printe("Unhandled error in playback stats tracking: $throwable")
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default + exceptionHandler)

    @Volatile
    private var resolveJob: Job? = null

    @Volatile
    private var trackingJob: Job? = null

    fun stopPlaybackTracking() {
        resolveJob?.cancel()
        resolveJob = null
        trackingJob?.cancel()
        trackingJob = null
    }

    fun onPlaybackStarted(
        videoId: String,
        settings: UmihiSettings,
    ) {
        stopPlaybackTracking()

        if (!settings.sendPlaybackData || settings.cookies.isEmpty()) {
            LogHelper.printd("Playback tracking skipped: sendPlaybackData=${settings.sendPlaybackData}, hasCookies=${settings.cookies.isNotEmpty()}")
            return
        }

        resolveJob = scope.launch {
            try {
                val trackingUrls = resolveTrackingUrls(videoId, settings) ?: run {
                    LogHelper.printe("No tracking URLs in player response for $videoId")
                    return@launch
                }

                if (!isActive) {
                    return@launch
                }

                val referrer = "${Constants.YoutubeApi.ORIGIN}/watch?v=$videoId"

                startPlaybackTracking(
                    videoId = videoId,
                    playbackUrl = trackingUrls.playbackUrl!!,
                    watchtimeUrl = trackingUrls.watchtimeUrl!!,
                    clientName = trackingUrls.clientName,
                    settings = settings,
                    playlistId = null,
                    referrer = referrer,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                LogHelper.printe(
                    "Failed to start playback tracking for $videoId: ${e.message}",
                    exception = e
                )
            }
        }
    }

    private suspend fun resolveTrackingUrls(
        videoId: String,
        settings: UmihiSettings,
    ): PlaybackTrackingUrls? {
        for (client in Constants.YoutubeApi.Client.TRACKING_FALLBACK_ORDER) {
            val clientName = client.getClientName()

            val playerResponse = try {
                YoutubeApiClient.getPlayerInfo(
                    videoId = videoId, client = client, visitorData = visitorData,
                    settings = settings,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                LogHelper.printw(
                    "[$clientName] Player request failed for $videoId: ${e.message}; " +
                            "trying next client"
                )
                continue
            }

            if (!coroutineContext.isActive) {
                return null
            }

            val trackingUrls = extractTrackingUrls(playerResponse, clientName)
            if (trackingUrls.playbackUrl != null && trackingUrls.watchtimeUrl != null) {
                return trackingUrls
            }

            LogHelper.printw(
                "[$clientName] No tracking URLs for $videoId " +
                        "(playability=${extractPlayability(playerResponse)}); trying next client"
            )
        }

        return null
    }


    private fun startPlaybackTracking(
        videoId: String,
        playbackUrl: String,
        watchtimeUrl: String,
        clientName: String,
        settings: UmihiSettings,
        playlistId: String?,
        referrer: String?,
    ) {
        stopPlaybackTracking()

        val cpn = UmihiHelper.Cpn.generate()

        scope.launch {
            sendInitPlayback(
                baseUrl = playbackUrl, cpn = cpn, clientName = clientName, settings = settings,
                playlistId = playlistId, referrer = referrer,
            )
            LogHelper.printd("Playback tracking started for $videoId (cpn=$cpn, client=$clientName)")
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
                            baseUrl = watchtimeUrl, cpn = cpn, clientName = clientName,
                            st = st, et = et,
                            settings = settings, playlistId = playlistId, referrer = referrer,
                        )
                        LogHelper.printd("Watchtime update: $st → $et ($videoId)")

                        lastReportedPosition = posSec
                    } else {
                        sendWatchtimeComplete(
                            baseUrl = watchtimeUrl, cpn = cpn, clientName = clientName,
                            durationSeconds = durSec,
                            settings = settings, playlistId = playlistId, referrer = referrer,
                        )
                        LogHelper.printd("Watchtime complete: $durSec ($videoId)")

                        stopPlaybackTracking()
                    }
                }
            }
        }
    }

    private fun extractPlayability(jsonString: String): String {
        return try {
            val status =
                JSONObject(jsonString).optJSONObject("playabilityStatus") ?: return "unknown"
            val code = status.optString("status").ifBlank { "unknown" }
            val reason = status.optString("reason").ifBlank { null }
            if (reason != null) "$code ($reason)" else code
        } catch (_: Exception) {
            "unparseable"
        }
    }

    private fun extractTrackingUrls(
        jsonString: String,
        clientName: String,
    ): PlaybackTrackingUrls {
        return try {
            val root = JSONObject(jsonString)
            val tracking = root.optJSONObject("playbackTracking")

            if (tracking == null) {
                LogHelper.printw(
                    "[$clientName] Missing playbackTracking; " +
                            "topLevelKeys=${root.keys().asSequence().toList()}"
                )
                return PlaybackTrackingUrls(null, null, clientName)
            }

            val playback = tracking.optJSONObject("videostatsPlaybackUrl")
                ?.optString("baseUrl")
                ?.takeIf { it.isNotBlank() }

            val watchtime = tracking.optJSONObject("videostatsWatchtimeUrl")
                ?.optString("baseUrl")
                ?.takeIf { it.isNotBlank() }

            if (playback == null || watchtime == null) {
                LogHelper.printw(
                    "[$clientName] Incomplete playbackTracking: " +
                            "playback=${playback != null}, " +
                            "watchtime=${watchtime != null}, " +
                            "keys=${tracking.keys().asSequence().toList()}"
                )
            }

            PlaybackTrackingUrls(playback, watchtime, clientName)
        } catch (e: Exception) {
            LogHelper.printe(
                "Failed to parse player response for tracking URLs: ${e.message}",
                exception = e
            )
            PlaybackTrackingUrls(null, null, clientName)
        }
    }

    private suspend fun sendInitPlayback(
        baseUrl: String,
        cpn: String,
        clientName: String,
        settings: UmihiSettings,
        playlistId: String? = null,
        referrer: String? = null,
    ): Int? {
        if (!settings.canTrack) {
            LogHelper.printd("Playback stats request skipped: canTrack=false")
            return null
        }

        return withContext(Dispatchers.IO) {
            val url =
                buildUrl(baseUrl, cpn, clientName, playlistId, referrer)?.build()
                    ?: return@withContext null
            val request = Request.Builder()
                .url(url)
                .post(FormBody.Builder().build())
                .applyHeaders(url, settings)
                .build()

            executeRequest(request)
        }
    }

    private suspend fun sendWatchtimeUpdate(
        baseUrl: String,
        cpn: String,
        clientName: String,
        st: String,
        et: String,
        settings: UmihiSettings,
        playlistId: String? = null,
        referrer: String? = null,
    ): Int? {
        if (!settings.canTrack) {
            LogHelper.printd("Playback stats request skipped: canTrack=false")
            return null
        }

        return withContext(Dispatchers.IO) {
            val url = buildUrl(baseUrl, cpn, clientName, playlistId, referrer)
                ?.addEncodedQueryParameter("st", st)
                ?.addEncodedQueryParameter("et", et)
                ?.build()
                ?: return@withContext null

            val request = Request.Builder()
                .url(url)
                .post(FormBody.Builder().build())
                .applyHeaders(url, settings)
                .build()

            executeRequest(request)
        }
    }

    private suspend fun sendWatchtimeComplete(
        baseUrl: String,
        cpn: String,
        clientName: String,
        durationSeconds: Float,
        settings: UmihiSettings,
        playlistId: String? = null,
        referrer: String? = null,
    ): Int? {
        val durationStr = durationSeconds.formatDecimal()
        return sendWatchtimeUpdate(
            baseUrl = baseUrl,
            cpn = cpn,
            clientName = clientName,
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
        clientName: String,
        playlistId: String?,
        referrer: String?,
    ): HttpUrl.Builder? {
        val builder = baseUrl.toHttpUrlOrNull()?.newBuilder()
        if (builder == null) {
            LogHelper.printe("Invalid playback tracking URL: $baseUrl")
            return null
        }

        builder.addEncodedQueryParameter("cpn", cpn)
        builder.addEncodedQueryParameter("ver", "2")
        builder.addEncodedQueryParameter("c", clientName)

        playlistId?.let { builder.addEncodedQueryParameter("list", it) }
        referrer?.let { builder.addEncodedQueryParameter("referrer", it) }

        return builder
    }


    private fun executeRequest(request: Request): Int? {
        return try {
            UmihiHttpClient.client.newCall(request).execute().use { response ->
                val queryPreview = (request.url.encodedQuery ?: "").take(80)
                LogHelper.printd("PlaybackStats: ${request.url.encodedPath}?$queryPreview... -> ${response.code}")
                response.code
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LogHelper.printe("PlaybackStats request failed: ${e.message}", exception = e)
            null
        }
    }
}