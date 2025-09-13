package ca.ilianokokoro.umihi.music.ui.screens.player.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.components.SongListItem
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueBottomSheet(
    changeVisibility: (visible: Boolean) -> Unit,
    scope: CoroutineScope,
    sheetState: SheetState,
    songs: List<Song>,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = {
            changeVisibility(false)
        },
        sheetState = sheetState
    ) {

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
        ) {
            if (songs.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.empty_playlist), // TODO : make the text vertically centered
                        textAlign = TextAlign.Center,
                        modifier = modifier
                            .fillMaxSize()
                    )
                }
            } else {
                items(
                    count = songs.size,
                    key = { songIndex ->
                        val song = songs[songIndex]
                        "song_${song.id}_$songIndex" // TODO make it cleaner later
                    },
                ) { songIndex ->
                    val song = songs[songIndex]
                    SongListItem(song, onPress = {
                        // TODO: skip to it
                    })
                }
            }

        }


//        Button(onClick = {
//            scope.launch { sheetState.hide() }
//                .invokeOnCompletion {
//                    if (!sheetState.isVisible) {
//                        changeVisibility(false)
//                    }
//                }
//        }) {
//            Text("Hide bottom sheet")
//        }
    }

}