package ca.ilianokokoro.umihi.music.ui.screens.player


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import ca.ilianokokoro.umihi.music.models.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(song: Song, application: Application, player: Player) :
    AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(PlayerState(song))
    val uiState = _uiState.asStateFlow()

    private val _player = player

    init {
        Log.d("CustomLog", "init PlayerViewModel")
        playTrack("https://storage.googleapis.com/exoplayer-test-media-0/play.mp3")
    }

    companion object {
        fun Factory(
            song: Song,
            application: Application,
            player: Player
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    PlayerViewModel(song, application, player)
                }
            }
    }

    fun playTrack(url: String) {
        viewModelScope.launch {
            _player.setMediaItem(MediaItem.fromUri(url))
            _player.prepare()
            _player.play()
        }
    }
}