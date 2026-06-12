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
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.services.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.cancellation.CancellationException

object PlayerManager {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _controllerState = MutableStateFlow<MediaController?>(null)
    val controllerState: StateFlow<MediaController?> = _controllerState.asStateFlow()

    private val isConnected: Boolean
        get() = controller?.isConnected == true

    val currentController: MediaController?
        get() = controller?.takeIf { it.isConnected }


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

                    controller = built
                    _controllerState.value = built

                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    clearDeadController()
                } finally {
                    controllerFuture = null
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
        currentController?.seekToNextMediaItem()
    }

    fun skipToPrevious() {
        currentController?.seekToPreviousMediaItem()
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