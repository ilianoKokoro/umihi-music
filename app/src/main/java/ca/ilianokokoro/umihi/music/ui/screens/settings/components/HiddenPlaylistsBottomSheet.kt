package ca.ilianokokoro.umihi.music.ui.screens.settings.components

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.ui.components.playlist.PlaylistListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenPlaylistsBottomSheet(
    playlists: List<Playlist>,
    onUnhidePlaylist: (Playlist) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Expanded,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    val handler = remember { Handler(Looper.getMainLooper()) }

    ModalBottomSheet(
        onDismissRequest = {
            handler.post { onDismiss() }
        },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.hidden_playlists),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when {
                playlists.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.no_hidden_playlists),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp)
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            bottom = Constants.Ui.SCROLLABLE_BOTTOM_PADDING
                        ),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(
                            items = playlists,
                            key = { it.info.id }
                        ) { playlist ->
                            PlaylistListItem(
                                playlist = playlist,
                                onUnhidePlaylist = { onUnhidePlaylist(playlist) }
                            )
                        }
                    }
                }
            }
        }
    }
}
