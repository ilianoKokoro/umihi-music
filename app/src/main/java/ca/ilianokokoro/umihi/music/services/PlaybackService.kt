package ca.ilianokokoro.umihi.music.services

import android.app.PendingIntent
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSourceBitmapLoader
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.CacheBitmapLoader
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.ExoCache
import ca.ilianokokoro.umihi.music.core.datasources.YoutubeDataSourceFactory
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printe
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import ca.ilianokokoro.umihi.music.data.repositories.PlaylistRepository
import ca.ilianokokoro.umihi.music.data.repositories.SongRepository
import ca.ilianokokoro.umihi.music.extensions.cappedTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

@UnstableApi
class PlaybackService : MediaLibraryService() {
    private var mediaLibrarySession: MediaLibrarySession? = null
    private lateinit var exoCache: ExoCache
    private lateinit var player: Player
    private lateinit var datastoreRepository: DatastoreRepository
    private val songRepository = SongRepository()
    private val playlistRepository = PlaylistRepository()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var callback: UmihiMediaLibraryCallback


    override fun onCreate() {
        super.onCreate()

        datastoreRepository = DatastoreRepository(applicationContext)
        exoCache = ExoCache(application)

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(Util.getUserAgent(this, packageName))

        val defaultDataSourceFactory = DefaultDataSource.Factory(this, httpDataSourceFactory)

        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(exoCache.cache)
            .setUpstreamDataSourceFactory(defaultDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        val resolvingFactory = YoutubeDataSourceFactory(application, cacheDataSourceFactory)

        val settings = runBlocking { datastoreRepository.settings.first() }

        val audioOffloadMode = if (settings.useAudioOffload) {
            TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_ENABLED
        } else {
            TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_DISABLED
        }

        val audioOffloadPreferences =
            TrackSelectionParameters.AudioOffloadPreferences.Builder()
                .setAudioOffloadMode(audioOffloadMode)
                .setIsGaplessSupportRequired(true)
                .setIsSpeedChangeSupportRequired(true)
                .build()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setHandleAudioBecomingNoisy(true)
            .setDeviceVolumeControlEnabled(true)
            .setMediaSourceFactory(DefaultMediaSourceFactory(resolvingFactory))
            .build()

        player.trackSelectionParameters =
            player.trackSelectionParameters
                .buildUpon()
                .setAudioOffloadPreferences(audioOffloadPreferences)
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

        callback = UmihiMediaLibraryCallback(
            service = this,
            serviceScope = serviceScope,
            datastoreRepository = datastoreRepository,
            songRepository = songRepository,
            playlistRepository = playlistRepository
        )

        mediaLibrarySession = MediaLibrarySession.Builder(this, player, callback)
            .setSessionActivity(pendingIntent)
            .setBitmapLoader(CacheBitmapLoader(DataSourceBitmapLoader.Builder(this).build()))
            .build()
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaLibrarySession? = mediaLibrarySession

    override fun onTaskRemoved(rootIntent: android.content.Intent?) {
        val player = mediaLibrarySession?.player
        if (player == null || player.mediaItemCount == 0) {
            pauseAllPlayersAndStopSelf()
        }
    }

    override fun onDestroy() {
        mediaLibrarySession?.run {
            player.release()
            exoCache.release()
            release()
            mediaLibrarySession = null
        }
        super.onDestroy()
    }

    private fun updateCurrentMediaItemThumbnail(mediaItem: MediaItem?) {
        if (mediaItem == null) return

        val context = applicationContext
        val songId = mediaItem.mediaId

        serviceScope.launch {
            try {
                val imageDir = UmihiHelper.getDownloadDirectory(
                    context,
                    Constants.Downloads.THUMBNAILS_FOLDER
                )

                val downloadedImage = File(imageDir, "$songId.jpg")
                if (downloadedImage.exists()) {
                    val imageBytes = downloadedImage.readBytes()

                    updateMediaItemArtwork(
                        mediaItem,
                        imageBytes.cappedTo(),
                        downloadedImage.toUri()
                    )
                    return@launch
                }

                songRepository.getSongInfo(songId)
                    .collect { result ->
                        when (result) {
                            is ApiResult.Success -> {
                                val song = result.data
                                val thumbnail = song.thumbnailHref
                                if (thumbnail.isNotBlank()) {
                                    val artBytes = UmihiHelper.fetchArtworkBytes(thumbnail)
                                    if (artBytes != null) {
                                        updateMediaItemArtwork(
                                            mediaItem,
                                            artBytes,
                                            song.thumbnailHref.toUri()
                                        )
                                    }
                                    return@collect
                                }
                            }

                            is ApiResult.Error -> {
                                error("Request to YouTube failed")
                            }

                            else -> {}
                        }
                    }
            } catch (ex: Exception) {
                printe(
                    message = "Failed to get full res thumbnail for $songId. Error : ${ex.message}",
                )
            }
        }
    }

    private suspend fun updateMediaItemArtwork(
        mediaItem: MediaItem,
        artBytes: ByteArray?,
        uri: Uri
    ) {
        val extras = mediaItem.mediaMetadata.extras
        extras?.putString(
            Constants.ExoPlayer.SongMetadata.UID,
            UUID.randomUUID().toString()
        )

        val updated = mediaItem.buildUpon()
            .setMediaMetadata(
                mediaItem.mediaMetadata.buildUpon()
                    .setArtworkData(artBytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                    .setArtworkUri(uri)
                    .setExtras(extras)
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
}