package ca.ilianokokoro.umihi.music.ui.screens.player


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.C
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
        PlayerManager.currentController?.seekTo(_uiState.value.progressMs.toLong())
    }

    fun seek(location: Float) {
        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(
                    progressMs = location,
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
                    val pos = controller?.currentPosition?.toFloat()

                    if (pos != null && pos != state.progressMs) {
                        _uiState.update { it.copy(progressMs = pos) }
                    }
                }

                delay(Constants.Player.PROGRESS_UPDATE_DELAY)
            }
        }
    }

    private fun updateSongDuration() {
        if (_uiState.value.durationMs != 0f) {
            return
        }

        viewModelScope.launch {
            var songDuration = PlayerManager.currentController?.duration ?: 0

            if (songDuration == C.TIME_UNSET) {
                songDuration = 0
            }

            _uiState.update {
                _uiState.value.copy(
                    durationMs = songDuration.toFloat(),
                )
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
                    updateSongDuration()
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
                    durationMs = 0f,
                    progressMs = 0f,
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