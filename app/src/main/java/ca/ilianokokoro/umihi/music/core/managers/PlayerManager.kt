package ca.ilianokokoro.umihi.music.core.managers

import android.content.ComponentName
import android.content.Context
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.extensions.toSong
import ca.ilianokokoro.umihi.music.models.PlaybackAudioInfo
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.services.PlaybackService
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

    fun updatePlaybackInfo(info: PlaybackAudioInfo) {
        _audioInfo.value = info
    }

    @OptIn(UnstableApi::class)
    @Synchronized
    fun connectController(context: Context) {
        if (isConnected) {
            _controllerState.value = controller
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

        if (mediaItems.isEmpty()) return

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
        controller = null
        _controllerState.value = null
    }
}