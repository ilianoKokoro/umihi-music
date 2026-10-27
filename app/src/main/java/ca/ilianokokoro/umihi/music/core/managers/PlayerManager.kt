package ca.ilianokokoro.umihi.music.core.managers

import android.content.ComponentName
import android.content.Context
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.audio.PlaybackService
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.data.datasources.SongDataSource
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import ca.ilianokokoro.umihi.music.extensions.toSong
import ca.ilianokokoro.umihi.music.models.PlaybackAudioInfo
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.Song
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object PlayerManager {
    @Volatile
    private var controllerFuture: ListenableFuture<MediaController>? = null

    @Volatile
    private var controller: MediaController? = null

    private val _controllerState = MutableStateFlow<MediaController?>(null)
    val controllerState: StateFlow<MediaController?> = _controllerState.asStateFlow()

    private val _currentSongId = MutableStateFlow<String?>(null)
    val currentSongId: StateFlow<String?> = _currentSongId.asStateFlow()

    private var currentSongListenerController: MediaController? = null
    private val currentSongListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _currentSongId.value = mediaItem?.mediaId
        }
    }

    val currentController: MediaController?
        get() = controller?.takeIf { it.isConnected }

    private val isConnected: Boolean
        get() = controller?.isConnected == true


    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var sleepTimerJob: Job? = null
    private var sleepTimerEndOfSongListener: Player.Listener? = null

    private val _sleepTimerRemainingSeconds = MutableStateFlow<Long?>(null)
    val sleepTimerRemainingSeconds: StateFlow<Long?> = _sleepTimerRemainingSeconds.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _audioInfo = MutableStateFlow(PlaybackAudioInfo())
    val audioInfo = _audioInfo.asStateFlow()

    private var playbackService: PlaybackService? = null

    private val _appVolume = MutableStateFlow(Constants.Player.Volume.DEFAULT_PERCENT)
    val appVolume: StateFlow<Int> = _appVolume.asStateFlow()

    private val _isMixActive = MutableStateFlow(false)
    val isMixActive: StateFlow<Boolean> = _isMixActive.asStateFlow()

    private val _isMixLoading = MutableStateFlow(false)
    val isMixLoading: StateFlow<Boolean> = _isMixLoading.asStateFlow()

    private val _isMixLoadingMore = MutableStateFlow(false)
    val isMixLoadingMore: StateFlow<Boolean> = _isMixLoadingMore.asStateFlow()

    private var mixContinuationToken: String? = null
    private var appContext: Context? = null

    private var mixListenerController: MediaController? = null
    private val mixListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            maybeLoadMoreMix()
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            maybeLoadMoreMix()
        }
    }

    fun registerPlaybackService(service: PlaybackService) {
        playbackService = service
    }

    fun unregisterPlaybackService() {
        playbackService = null
    }

    fun setInitialVolume(volume: Int) {
        _appVolume.value = volume
    }

    fun setAppVolume(volume: Int, context: Context) {
        val clamped = volume.coerceIn(
            Constants.Player.Volume.MIN_PERCENT,
            Constants.Player.Volume.MAX_PERCENT
        )
        _appVolume.value = clamped
        playbackService?.applyAppVolume(clamped)
        scope.launch {
            DatastoreRepository(context.applicationContext).save(
                DatastoreRepository.PreferenceKeys.APP_VOLUME,
                clamped
            )
        }
    }

    fun updatePlaybackInfo(info: PlaybackAudioInfo) {
        _audioInfo.value = info
    }

    @Synchronized
    fun connectController(context: Context) {
        appContext = context.applicationContext

        if (isConnected) {
            controller?.let { connectedController ->
                _controllerState.value = connectedController
                observeMix(connectedController)
                observeCurrentSong(connectedController)
            }
            return
        }

        if (controllerFuture != null) {
            return
        }

        clearDeadController()

        val appContext = context.applicationContext

        val sessionToken = SessionToken(
            appContext,
            ComponentName(appContext, PlaybackService::class.java)
        )

        val future = MediaController.Builder(appContext, sessionToken).buildAsync()
        controllerFuture = future

        future.addListener(
            {
                try {
                    val built = future.get()

                    synchronized(this@PlayerManager) {
                        controller = built
                        _controllerState.value = built
                        observeMix(built)
                        observeCurrentSong(built)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    clearDeadController()
                } finally {
                    synchronized(this@PlayerManager) {
                        controllerFuture = null
                    }
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    @Synchronized
    fun disconnectController() {
        controllerFuture?.let { future ->
            MediaController.releaseFuture(future)
        }

        controllerFuture = null

        controller?.release()
        clearDeadController()
    }

    val playbackState: Int
        get() = currentController?.playbackState ?: Player.STATE_IDLE

    val isPlaying: Boolean
        get() = currentController?.isPlaying == true

    val repeatMode: Int
        get() = currentController?.repeatMode ?: Player.REPEAT_MODE_OFF

    fun seekToIndex(index: Int, positionMs: Long = C.TIME_UNSET) {
        currentController?.seekTo(index, positionMs)
    }

    fun skipToNext() {
        currentController?.run {
            seekToNextMediaItem()
            prepare()
        }
    }

    fun skipToPrevious() {
        currentController?.run {
            seekToPreviousMediaItem()
            prepare()
        }
    }

    fun cycleRepeatMode() {
        val nextRepeatMode = when (repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
            else -> Player.REPEAT_MODE_OFF
        }

        setRepeatMode(nextRepeatMode)
    }


    fun playMediaItem(mediaItem: MediaItem) {
        currentController?.run {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }


    fun playPlaylist(playlist: Playlist, index: Int = 0) {
        val controller = currentController ?: return
        val mediaItems = playlist.mediaItems

        if (mediaItems.isEmpty()) {
            return
        }

        controller.setMediaItems(
            mediaItems,
            index.coerceIn(0, mediaItems.lastIndex),
            C.TIME_UNSET
        )

        controller.prepare()
        controller.play()
    }

    fun shufflePlaylist(playlist: Playlist) {
        val shuffledPlaylist = playlist.copy(
            songs = playlist.songs.shuffled()
        )

        playPlaylist(shuffledPlaylist)
    }


    fun playQueue(
        mediaItems: List<MediaItem>,
        startIndex: Int = 0,
        startPositionMs: Long = 0L
    ) {
        if (mediaItems.isEmpty()) {
            return
        }

        currentController?.run {
            setMediaItems(
                mediaItems,
                startIndex.coerceIn(0, mediaItems.lastIndex),
                startPositionMs
            )
            prepare()
            play()
        }
    }

    fun playSong(song: Song) {
        val controller = currentController ?: return

        controller.setMediaItem(song.mediaItem)
        controller.prepare()
        controller.play()
    }

    fun toggleMix(context: Context) {
        if (_isMixActive.value) {
            disableMix()
        } else {
            getCurrentSong()?.let { playMix(it, context) }
        }
    }

    fun disableMix() {
        _isMixActive.value = false
        mixContinuationToken = null

        currentController?.let { controller ->
            removeMixItems(controller)
        }
    }

    fun playMix(song: Song, context: Context) {
        if (_isMixLoading.value) {
            return
        }

        val appContext = context.applicationContext
        this.appContext = appContext
        val seedIsCurrent = currentController?.currentMediaItem?.mediaId == song.youtubeId

        _isMixLoading.value = true

        scope.launch {
            try {
                val settings = DatastoreRepository(appContext).getSettings()
                check(!settings.offlineMode)

                val page = SongDataSource().getMix(song.youtubeId, settings)
                check(page.songs.isNotEmpty())

                withContext(Dispatchers.Main.immediate) {
                    val controller = currentController ?: return@withContext

                    removeMixItems(controller)

                    if (!seedIsCurrent) {
                        controller.setMediaItem(song.mediaItem)
                        controller.prepare()
                        controller.play()
                    }

                    val existingIds = (0 until controller.mediaItemCount)
                        .mapNotNull { controller.getMediaItemAt(it).mediaId }
                        .toHashSet()

                    val mixItems = page.songs.asSequence()
                        .filterNot { it.youtubeId == song.youtubeId || it.youtubeId in existingIds }
                        .distinctBy { it.youtubeId }
                        .map { it.apply { isMix = true }.mediaItem }
                        .toList()

                    if (mixItems.isNotEmpty()) {
                        controller.addMediaItems(controller.mediaItemCount, mixItems)
                    }

                    mixContinuationToken = page.continuation
                    _isMixActive.value = true

                    Toast.makeText(
                        appContext,
                        appContext.getString(R.string.mix_started_toast),
                        Toast.LENGTH_SHORT
                    ).show()

                    maybeLoadMoreMix()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                withContext(Dispatchers.Main.immediate) {
                    Toast.makeText(
                        appContext,
                        appContext.getString(R.string.mix_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                _isMixLoading.value = false
            }
        }
    }

    private fun maybeLoadMoreMix() {
        if (!_isMixActive.value) {
            return
        }
        if (isMixLoadingMore.value) {
            return
        }
        val continuation = mixContinuationToken ?: return
        val context = appContext ?: return
        val controller = currentController ?: return

        val index = controller.currentMediaItemIndex
        if (index < 0) {
            return
        }

        val remaining = controller.mediaItemCount - 1 - index
        if (remaining <= MIX_LOAD_MORE_THRESHOLD) {
            loadMoreMix(context, continuation)
        }
    }

    private fun loadMoreMix(context: Context, continuation: String) {
        _isMixLoadingMore.value = true

        scope.launch {
            try {
                val settings = DatastoreRepository(context.applicationContext).getSettings()
                if (settings.offlineMode) {
                    return@launch
                }

                val page = SongDataSource().getMixContinuation(continuation, settings)

                withContext(Dispatchers.Main.immediate) {
                    if (!_isMixActive.value || mixContinuationToken != continuation) {
                        return@withContext
                    }

                    mixContinuationToken = page.continuation

                    val controller = currentController ?: return@withContext
                    val existingIds = (0 until controller.mediaItemCount)
                        .mapNotNull { controller.getMediaItemAt(it).mediaId }
                        .toHashSet()

                    val mixItems = page.songs.asSequence()
                        .filterNot { it.youtubeId in existingIds }
                        .distinctBy { it.youtubeId }
                        .map { it.apply { isMix = true }.mediaItem }
                        .toList()

                    if (mixItems.isNotEmpty()) {
                        controller.addMediaItems(controller.mediaItemCount, mixItems)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Silent: mix keeps playing; more songs load on next transition.
            } finally {
                _isMixLoadingMore.value = false
            }
        }
    }

    private fun removeMixItems(controller: MediaController) {
        val count = controller.mediaItemCount
        val firstMixIndex = (0 until count).firstOrNull { index ->
            isMixMediaItem(controller, index)
        } ?: return

        controller.removeMediaItems(firstMixIndex, count)
    }

    private fun isMixMediaItem(controller: MediaController, index: Int): Boolean {
        return controller.getMediaItemAt(index)
            .mediaMetadata.extras
            ?.getBoolean(Constants.ExoPlayer.SongMetadata.IS_MIX) == true
    }

    private fun observeMix(controller: MediaController) {
        if (mixListenerController !== controller) {
            mixListenerController?.removeListener(mixListener)
            mixListenerController = controller
            controller.addListener(mixListener)
        }
    }


    suspend fun getPlaybackPosition(): Pair<Float, Float>? {
        return withContext(Dispatchers.Main.immediate) {
            val controller = currentController ?: return@withContext null
            val positionMs = controller.currentPosition
            val durationMs = controller.duration
            if (positionMs <= 0 || durationMs <= 0) {
                return@withContext null
            }
            (positionMs / 1000f) to (durationMs / 1000f)
        }
    }

    fun addNext(song: Song, context: Context? = null) {
        val controller = currentController ?: return

        val insertIndex = if (controller.mediaItemCount == 0) {
            0
        } else {
            (controller.currentMediaItemIndex + 1)
                .coerceIn(0, controller.mediaItemCount)
        }

        controller.addMediaItem(insertIndex, song.mediaItem)

        context?.let {
            Toast.makeText(
                it,
                it.getString(R.string.play_next_toast),
                Toast.LENGTH_SHORT
            ).show()
        }

        playIfFirstQueueItem()
    }

    fun addToQueue(song: Song, context: Context? = null) {
        val controller = currentController ?: return

        controller.addMediaItem(controller.mediaItemCount, song.mediaItem)

        context?.let {
            Toast.makeText(
                it,
                it.getString(R.string.added_queue_toast),
                Toast.LENGTH_SHORT
            ).show()
        }

        playIfFirstQueueItem()
    }

    fun removeMediaItem(index: Int) {
        val controller = currentController ?: return

        if (index in 0 until controller.mediaItemCount) {
            controller.removeMediaItem(index)
        }
    }

    fun shuffleQueue(context: Context? = null) {
        val controller = currentController ?: return

        val currentIndex = controller.currentMediaItemIndex
        if (currentIndex < 0 || controller.mediaItemCount <= 1) {
            return
        }

        if (currentIndex != 0) {
            controller.moveMediaItem(currentIndex, 0)
        }

        val trailing = (1 until controller.mediaItemCount)
            .map(controller::getMediaItemAt)

        controller.removeMediaItems(1, controller.mediaItemCount)
        controller.addMediaItems(trailing.shuffled())

        context?.let {
            Toast.makeText(
                it,
                it.getString(R.string.shuffled_queue_toast),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun clearQueue() {
        currentController?.run {
            stop()
            clearMediaItems()
        }
    }

    @Synchronized
    fun getQueue(): List<Song> {
        val controller = currentController ?: return emptyList()
        val queue = mutableListOf<Song>()

        for (i in 0 until controller.mediaItemCount) {
            queue.add(controller.getMediaItemAt(i).toSong())
        }

        return queue
    }

    @Synchronized
    fun getCurrentSong(): Song? {
        return currentController?.currentMediaItem?.toSong()
    }

    @Synchronized
    fun getCurrentIndex(): Int {
        return currentController?.currentMediaItemIndex ?: C.INDEX_UNSET
    }

    @OptIn(UnstableApi::class)
    fun setAudioOffloadEnabled(value: Boolean) {
        val controller = currentController ?: return

        val mode = if (value) {
            TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_ENABLED
        } else {
            TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_DISABLED
        }

        controller.trackSelectionParameters =
            controller.trackSelectionParameters
                .buildUpon()
                .setAudioOffloadPreferences(
                    controller.trackSelectionParameters.audioOffloadPreferences
                        .buildUpon()
                        .setAudioOffloadMode(mode)
                        .build()
                )
                .build()
    }

    private fun setRepeatMode(repeatMode: Int) {
        currentController?.repeatMode = repeatMode
    }


    fun startSleepTimer(minutes: Int) {
        startSleepTimerRemaining(minutes * 60L)
    }

    fun startSleepTimerEndOfSong() {
        cancelSleepTimer()

        val controller = currentController ?: return

        val listener = object : Player.Listener {
            override fun onMediaItemTransition(
                mediaItem: MediaItem?,
                reason: Int
            ) {
                if (
                    reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO ||
                    reason == Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT
                ) {
                    controller.pause()
                    cancelSleepTimer()
                }
            }
        }

        controller.addListener(listener)
        sleepTimerEndOfSongListener = listener

        sleepTimerJob = scope.launch {
            while (isActive) {
                val remainingSeconds =
                    withContext(Dispatchers.Main.immediate) {
                        val durationMs = controller.duration
                        val positionMs = controller.currentPosition

                        if (
                            durationMs > 0
                        ) {
                            ((durationMs - positionMs) / 1000)
                                .coerceAtLeast(0)
                        } else {
                            -1L
                        }
                    }

                _sleepTimerRemainingSeconds.value = remainingSeconds

                delay(
                    if (remainingSeconds >= 0) {
                        250.milliseconds
                    } else {
                        1.seconds
                    }
                )
            }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        currentController?.setPlaybackSpeed(speed)
        _playbackSpeed.value = speed
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null

        sleepTimerEndOfSongListener?.let { listener ->
            currentController?.removeListener(listener)
        }
        sleepTimerEndOfSongListener = null
        _sleepTimerRemainingSeconds.value = null
    }

    private fun startSleepTimerRemaining(remainingSeconds: Long) {
        cancelSleepTimer()

        val endTimeMillis = System.currentTimeMillis() + (remainingSeconds * 1000)

        sleepTimerJob = scope.launch {
            while (isActive) {
                val millisRemaining =
                    (endTimeMillis - System.currentTimeMillis())
                        .coerceAtLeast(0)

                val remaining =
                    (millisRemaining + 999) / 1000

                _sleepTimerRemainingSeconds.value = remaining

                if (remaining <= 0) {
                    break
                }

                delay(250.milliseconds)
            }

            _sleepTimerRemainingSeconds.value = null

            withContext(Dispatchers.Main) {
                currentController?.pause()
            }
        }
    }


    private fun playIfFirstQueueItem() {
        val controller = currentController ?: return

        if (controller.mediaItemCount == 1) {
            controller.prepare()
            controller.play()
        }
    }

    @Synchronized
    private fun clearDeadController() {
        currentSongListenerController?.removeListener(currentSongListener)
        currentSongListenerController = null
        _currentSongId.value = null
        mixListenerController?.removeListener(mixListener)
        mixListenerController = null
        _isMixLoadingMore.value = false
        _isMixLoading.value = false
        mixContinuationToken = null
        _isMixActive.value = false
        controller = null
        _controllerState.value = null
    }

    private fun observeCurrentSong(controller: MediaController) {
        if (currentSongListenerController !== controller) {
            currentSongListenerController?.removeListener(currentSongListener)
            currentSongListenerController = controller
            controller.addListener(currentSongListener)
        }
        _currentSongId.value = controller.currentMediaItem?.mediaId
    }
    private const val MIX_LOAD_MORE_THRESHOLD = 5
}
