package ca.ilianokokoro.umihi.music.ui.screens.player


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import ca.ilianokokoro.umihi.music.core.Constants
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
                _uiState.update {
                    _uiState.value.copy(
                        isPlaying = _player.isPlaying // TODO : Add loading indicator
                    )
                }
            }
        })


        startProgressUpdate()
        updateCurrentSong()
    }

    private fun updateCurrentSong() {
        val currentItem = _player.currentMediaItem?.mediaMetadata
        val currentSong =
            Song("", currentItem?.title.toString(), currentItem?.artist.toString(), "")
        _uiState.update {
            _uiState.value.copy(
                currentSong = currentSong
            )
        }
    }

    private fun startProgressUpdate() {
        viewModelScope.launch {
            while (true) {
                _uiState.update {
                    _uiState.value.copy(
                        progressMs = _player.currentPosition
                    )
                }
                delay(Constants.Player.PROGRESS_UPDATE_DELAY)
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