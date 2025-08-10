package ca.ilianokokoro.umihi.music.ui.screens.player


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.MediaItem
import ca.ilianokokoro.umihi.music.PlayerApp
import ca.ilianokokoro.umihi.music.models.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(song: Song, application: Application) :
    AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(PlayerState(song))
    val uiState = _uiState.asStateFlow()

    private val app = application as PlayerApp
    val player = app.player


    init {
        Log.d("CustomLog", "init PlayerViewModel")
        playTrack("https://storage.googleapis.com/exoplayer-test-media-0/play.mp3")
    }

    companion object {
        fun Factory(song: Song, application: Application): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    PlayerViewModel(song, application)
                }
            }
    }

    fun playTrack(url: String) {
        viewModelScope.launch {
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.play()
        }
    }
}