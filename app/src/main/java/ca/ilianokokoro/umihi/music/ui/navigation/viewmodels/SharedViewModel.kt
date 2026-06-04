package ca.ilianokokoro.umihi.music.ui.navigation.viewmodels

import androidx.lifecycle.ViewModel
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class SharedViewModel : ViewModel() {

    private val _deletedPlaylistIds = MutableStateFlow<Set<String>>(emptySet())
    val deletedPlaylistIds = _deletedPlaylistIds.asStateFlow()

    fun markPlaylistDeleted(playlist: PlaylistInfo) {
        _deletedPlaylistIds.update { current ->
            current + playlist.id
        }
    }

    fun consumeDeletedPlaylists() {
        _deletedPlaylistIds.value = emptySet()
    }
}