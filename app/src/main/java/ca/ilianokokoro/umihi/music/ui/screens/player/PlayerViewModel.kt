package ca.ilianokokoro.umihi.music.ui.screens.player


import android.app.Application
import android.util.Log
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
                Log.d("CustomLog", "onIsPlayingChanged isPlaying $isPlaying")
                viewModelScope.launch {
                    _uiState.update {
                        _uiState.value.copy(
                            isPlaying = _player.isPlaying
                        )
                    }
                }
            }


            override fun onPlaybackStateChanged(playbackState: Int) {
                viewModelScope.launch {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            Log.d("CustomLog", "Player.STATE_BUFFERING")
                            _uiState.update {
                                _uiState.value.copy(
                                    isLoading = true
                                )
                            }
                        }

                        Player.STATE_READY -> {
                            Log.d("CustomLog", "Player.STATE_READY")
                            updateSongDuration()
                            _uiState.update {
                                _uiState.value.copy(
                                    isLoading = false
                                )
                            }
                        }

//                        Player.STATE_ENDED -> {
//                            Log.d("CustomLog", "Player.STATE_ENDED")
//                        }

//                        Player.STATE_IDLE -> {
//                            Log.d("CustomLog", "Player.STATE_IDLE")
//                        }


                        else -> {
                            // Do nothing
                        }
                    }
                }
            }
        })


        startProgressUpdate()
        updateCurrentSong()
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

    private fun updateCurrentSong() {
        Log.d("CustomLog", "updateCurrentSong ${_player.getCurrentSong()}")
        resetState()

        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(
                    currentSong = _player.getCurrentSong(),
                )
            }
        }
    }

    private fun startProgressUpdate() {
        Log.d("CustomLog", "startProgressUpdate progressMs ${_player.currentPosition.toFloat()}")
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


            Log.d(
                "CustomLog",
                "updateSongInfo durationMs $songDuration"
            )

            _uiState.update {
                _uiState.value.copy(
                    durationMs = songDuration.toFloat(),
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