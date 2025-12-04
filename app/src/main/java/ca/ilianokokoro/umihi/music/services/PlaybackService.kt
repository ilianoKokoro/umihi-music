package ca.ilianokokoro.umihi.music.services

import android.app.PendingIntent
import android.widget.Toast
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.ExoCache
import ca.ilianokokoro.umihi.music.core.factories.YoutubeMediaSourceFactory
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printe
import ca.ilianokokoro.umihi.music.data.repositories.SongRepository
import com.google.common.collect.ImmutableList
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

        val defaultDataSourceFactory = DefaultDataSource.Factory(this, httpDataSourceFactory)

        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(exoCache.cache)
            .setUpstreamDataSourceFactory(defaultDataSourceFactory)
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
            .setMediaSourceFactory(YoutubeMediaSourceFactory(application, cacheDataSourceFactory))
            .build()

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(
                mediaItem: MediaItem?,
                reason: Int
            ) {
                // Load the full res image when a new song is played
                updateCurrentMediaItemThumbnail(mediaItem)
            }

            // Show toast on error
            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_LONG).show()
            }
        })

        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()

        setMediaNotificationProvider(CustomMediaNotificationProvider(this))
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: android.content.Intent?) {
        val player = mediaSession?.player
        if (player != null) {
            if (!player.playWhenReady || player.mediaItemCount == 0) {
                stopSelf()
            }
        }
    }

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
                                if (result.data.isBlank()) {
                                    return@collect
                                }
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

                            is ApiResult.Error -> {
                                printe(message = result.errorMessage, exception = result.exception)
                            }

                            else -> Unit
                        }
                    }
                }
            } catch (ex: Exception) {
                printe(
                    message = "Failed to get full res thumbnail for ${mediaItem.mediaId}. Error : ${ex.message}",
                    exception = ex
                )
            }
        }
    }

    private class CustomMediaNotificationProvider(
        private val context: android.content.Context
    ) : MediaNotification.Provider {

        override fun createNotification(
            mediaSession: MediaSession,
            customLayout: ImmutableList<CommandButton>,
            actionFactory: MediaNotification.ActionFactory,
            onNotificationChangedCallback: MediaNotification.Provider.Callback
        ): MediaNotification {
            val builder = DefaultMediaNotificationProvider.Builder(context)
                .build()
            return builder.createNotification(
                mediaSession,
                customLayout,
                actionFactory,
                onNotificationChangedCallback
            )
        }

        override fun handleCustomCommand(
            session: MediaSession,
            action: String,
            extras: android.os.Bundle
        ): Boolean = false
    }
}