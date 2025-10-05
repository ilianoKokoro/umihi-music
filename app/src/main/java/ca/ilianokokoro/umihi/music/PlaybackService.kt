package ca.ilianokokoro.umihi.music

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.ExoCache
import ca.ilianokokoro.umihi.music.core.factories.YoutubeMediaSourceFactory
import ca.ilianokokoro.umihi.music.data.repositories.SongRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var exoCache: ExoCache
    private lateinit var player: Player
    private val songRepository = SongRepository()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    override fun onCreate() {
        super.onCreate()

        exoCache = ExoCache(application)

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(Util.getUserAgent(this, packageName))

        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(exoCache.cache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(), true
            )
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setHandleAudioBecomingNoisy(true)
            .setMediaSourceFactory(
                YoutubeMediaSourceFactory(application, cacheDataSourceFactory)
            )
            .build()

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(
                mediaItem: MediaItem?,
                reason: Int
            ) {
                // Load the full res image when a new song is played
                updateCurrentMediaItemThumbnail(mediaItem)
            }
        })

        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            exoCache.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private fun updateCurrentMediaItemThumbnail(mediaItem: MediaItem?) {
        if (mediaItem != null) {
            try {
                serviceScope.launch {
                    songRepository.getSongThumbnail(mediaItem.mediaId).collect { result ->
                        when (result) {
                            is ApiResult.Success -> {
                                val updated = mediaItem.buildUpon()
                                    .setMediaMetadata(
                                        mediaItem.mediaMetadata.buildUpon()
                                            .setArtworkUri(result.data.toUri())
                                            .build()
                                    )
                                    .build()

                                withContext(Dispatchers.Main) {
                                    if (player.currentMediaItem?.mediaId == mediaItem.mediaId) {
                                        player.replaceMediaItem(
                                            player.currentMediaItemIndex,
                                            updated
                                        )
                                    }

                                }
                            }

                            else -> Unit
                        }
                    }
                }
            } catch (ex: Exception) {
                Log.e(
                    "CustomLog",
                    "Failed to get full res thumbnail for ${mediaItem.mediaId}. Error : ${ex.message}",
                    ex
                )

            }
        }

    }
}