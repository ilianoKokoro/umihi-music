package ca.ilianokokoro.umihi.music

import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.factories.YoutubeMediaSourceFactory
import ca.ilianokokoro.umihi.music.data.repositories.SongRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: Player
    private val songRepository = SongRepository()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(Util.getUserAgent(this, this.packageName))

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(
                YoutubeMediaSourceFactory(httpDataSourceFactory)
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


        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
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
                    "Failed to get full res thumbnail for ${mediaItem.mediaId}. Error : ${ex.message}"
                )

            }
        }

    }
}