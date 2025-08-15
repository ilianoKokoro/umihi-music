package ca.ilianokokoro.umihi.music.ui.screens.player


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.extensions.getCurrentSong
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
                _uiState.update {
                    _uiState.value.copy(
                        isPlaying = _player.isPlaying
                    )
                }
            }

            override fun onIsLoadingChanged(isLoading: Boolean) {
                updateSongInfo() // TODO : Add loading indicator
            }
        })


        startProgressUpdate()
        updateCurrentSong()
    }

    fun seekPlayer() {
        _player.seekTo(_uiState.value.progressMs.toLong())
    }

    fun seek(location: Float) {
        _uiState.update {
            _uiState.value.copy(
                progressMs = location,
            )
        }
    }

    fun updateSeekBarHeldState(isHeld: Boolean) {
        if (_uiState.value.isSeekBarHeld == isHeld) {
            return
        }


        _uiState.update {
            _uiState.value.copy(
                isSeekBarHeld = isHeld,
            )
        }
    }

    fun pause() {
        _player.pause()
    }

    fun play() {
        _player.play()
    }

    private fun updateCurrentSong() {
        _uiState.update {
            _uiState.value.copy(
                currentSong = _player.getCurrentSong(),
            )
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

    private fun updateSongInfo() {
        var songDuration = _player.duration

        if (songDuration == C.TIME_UNSET) {
            songDuration = 0
        }

        _uiState.update {
            _uiState.value.copy(
                durationMs = songDuration.toFloat(),
                isLoading = _player.isLoading
            )
        }
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