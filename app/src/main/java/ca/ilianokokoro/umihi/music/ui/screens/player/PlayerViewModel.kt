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
import ca.ilianokokoro.umihi.music.extensions.getQueue
import ca.ilianokokoro.umihi.music.models.Song
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerViewModel(player: Player, application: Application) :
    AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(PlayerState())
    val uiState = _uiState.asStateFlow()
    private val _player = player

    init {
        _player.addListener(object : Player.Listener {
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
                updateCurrentSong()
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
        _player.seekTo(_uiState.value.progressMs.toLong())
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

    fun pause() {
        _player.pause()
    }

    fun play() {
        _player.play()
    }

    fun seekToNext() {
        _player.seekToNext()
    }

    fun seekToPrevious() {
        _player.seekToPrevious()
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
        viewModelScope.launch {
            resetState()

            _uiState.update {
                _uiState.value.copy(
                    currentIndex = _player.currentMediaItemIndex,
                    queue = _player.getQueue(),
                )
            }
        }
    }

    private fun startProgressUpdate() {
        viewModelScope.launch {
            while (true) {
                if (!_uiState.value.isSeekBarHeld) {
                    _uiState.update {
                        _uiState.value.copy(
                            progressMs = _player.currentPosition.toFloat(),
                        )
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
            var songDuration = _player.duration

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
            when (_player.playbackState) {
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
                    isPlaying = _player.isPlaying
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
        val artUri = _player.currentMediaItem?.mediaMetadata?.artworkUri ?: return
        if (currentSong?.thumbnailHref == artUri.toString()) {
            return
        }

        val newSong = currentSong?.copy(thumbnailHref = artUri.toString()) ?: return
        _uiState.value.queue[_uiState.value.currentIndex] = newSong
    }

    companion object {
        fun Factory(
            player: Player,
            application: Application,
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    PlayerViewModel(player, application)
                }
            }
    }
}