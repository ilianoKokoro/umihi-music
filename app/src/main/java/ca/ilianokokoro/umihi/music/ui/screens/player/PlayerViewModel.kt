package ca.ilianokokoro.umihi.music.ui.screens.player


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.managers.PlayerManager
import ca.ilianokokoro.umihi.music.extensions.getQueue
import ca.ilianokokoro.umihi.music.models.Song
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) :
    AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(PlayerState())
    val uiState = _uiState.asStateFlow()

    private var lastUpdatedSongIndex: Int = -1

    init {
        PlayerManager.currentController?.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateCurrentSong()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateIsPlayingState()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updateIsLoadingState()
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                updateQueue()
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                updateThumbnail()
            }
        })

        startProgressUpdate()
        updateCurrentSong()
        updateIsLoadingState()
        updateIsPlayingState()
    }


    fun seekPlayer() {
        PlayerManager.currentController?.seekTo(_uiState.value.playbackProgress.position.toLong())
    }

    fun seek(location: Float) {
        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(
                    playbackProgress = PlaybackProgress(
                        duration = it.playbackProgress.duration,
                        position = location,
                    ),
                )
            }
        }
    }

    fun updateSeekBarHeldState(isHeld: Boolean) {
        viewModelScope.launch {
            if (_uiState.value.isSeekBarHeld == isHeld) {
                return@launch
            }


            _uiState.update {
                _uiState.value.copy(
                    isSeekBarHeld = isHeld,
                )
            }
        }
    }

    fun setQueueVisibility(show: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(
                    isQueueModalShown = show
                )
            }
        }
    }

    private val currentSong: Song?
        get() = _uiState.value.queue.getOrNull(_uiState.value.currentIndex)


    private fun updateCurrentSong() {
        val newIndex = PlayerManager.currentController?.currentMediaItemIndex ?: return
        if (newIndex == lastUpdatedSongIndex) {
            return
        }
        lastUpdatedSongIndex = newIndex


        viewModelScope.launch {
            resetState()
            updateQueue()
        }

    }

    private fun updateQueue() {
        val controller = PlayerManager.currentController ?: return

        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(
                    currentIndex = controller.currentMediaItemIndex,
                    queue = controller.getQueue(),
                )
            }
        }
    }

    private fun startProgressUpdate() {
        viewModelScope.launch {
            while (true) {
                val state = _uiState.value

                if (!state.isSeekBarHeld && !state.isLoading) {
                    val controller = PlayerManager.currentController

                    val position = controller?.currentPosition?.toFloat()
                    val duration = controller?.duration?.toFloat()

                    val currentProgress = state.playbackProgress

                    val newPosition = position ?: currentProgress.position
                    val newDuration = duration ?: currentProgress.duration

                    if (
                        newPosition != currentProgress.position ||
                        newDuration != currentProgress.duration
                    ) {
                        _uiState.update {
                            it.copy(
                                playbackProgress = PlaybackProgress(
                                    position = newPosition,
                                    duration = newDuration
                                )
                            )
                        }
                    }
                }

                delay(Constants.Player.PROGRESS_UPDATE_DELAY)
            }
        }
    }

    private fun updateIsLoadingState() {
        viewModelScope.launch {
            when (PlayerManager.currentController?.playbackState) {
                Player.STATE_BUFFERING -> {
                    _uiState.update {
                        _uiState.value.copy(
                            isLoading = true
                        )
                    }
                }

                Player.STATE_READY -> {
                    _uiState.update {
                        _uiState.value.copy(
                            isLoading = false
                        )
                    }
                }

                else -> {
                }
            }
        }
    }

    private fun updateIsPlayingState() {
        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(
                    isPlaying = PlayerManager.currentController?.isPlaying == true
                )
            }
        }
    }


    private fun resetState() {
        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(
                    playbackProgress = PlaybackProgress(
                        duration = 0f,
                        position = 0f,
                    ),
                )
            }
        }
    }

    private fun updateThumbnail() {
        val artUri =
            PlayerManager.currentController?.currentMediaItem?.mediaMetadata?.artworkUri ?: return
        if (currentSong?.thumbnailHref == artUri.toString()) {
            return
        }

        val newSong = currentSong?.copy(thumbnailHref = artUri.toString()) ?: return
        _uiState.value.queue[_uiState.value.currentIndex] = newSong
    }

    companion object {
        fun Factory(
            application: Application,
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    PlayerViewModel(application)
                }
            }
    }
}