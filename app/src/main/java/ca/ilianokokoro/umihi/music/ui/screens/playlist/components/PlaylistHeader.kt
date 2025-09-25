package ca.ilianokokoro.umihi.music.ui.screens.playlist.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.ui.components.PlaylistInfo

@Composable
fun PlaylistHeader(
    modifier: Modifier = Modifier,
    onOpenPlayer: () -> Unit,
    onDownloadPlaylist: () -> Unit,
    onPlayPlaylist: () -> Unit,
    onShufflePlaylist: () -> Unit,
    playlist: Playlist
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        PlaylistInfo(
            playlist = playlist,
            onDownloadPressed = onDownloadPlaylist
        )
        ActionButtons(
            buttonEnabled = !playlist.songs.isEmpty(),
            onPlayClicked = {
                onOpenPlayer()
                onPlayPlaylist()
            },
            onShuffleClicked = {
                onOpenPlayer()
                onShufflePlaylist()
            }
        )
    }
}