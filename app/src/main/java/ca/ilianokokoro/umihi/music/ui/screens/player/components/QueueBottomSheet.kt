package ca.ilianokokoro.umihi.music.ui.screens.player.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Player
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.helpers.ComposeHelper
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.components.song.QueueSongListItem
import kotlinx.coroutines.CoroutineScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueBottomSheet(
    changeVisibility: (visible: Boolean) -> Unit,
    scope: CoroutineScope,
    player: Player,
    songs: List<Song>,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current

    var list by remember { mutableStateOf(listOf<Song>()) }

    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        list = list.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }

        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    LaunchedEffect(null) {
        Log.d("CustomLog", "Updating songs")
        list = songs
    }

    ModalBottomSheet(
        onDismissRequest = {
            changeVisibility(false)
        },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
        ) {
            Text(
                modifier = modifier.padding(start = 8.dp, bottom = 12.dp),
                text = stringResource(R.string.playing_now),
                style = MaterialTheme.typography.titleLarge
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (list.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.empty_playlist), // TODO : make the text vertically centered
                            textAlign = TextAlign.Center,
                            modifier = modifier
                                .fillMaxSize()
                        )
                    }
                } else {

                    itemsIndexed(
                        items = list,
                        key = { index, song -> ComposeHelper.getLazyKey(song, song.id, index) }
                    ) { index, song ->
                        ReorderableItem(
                            reorderableLazyListState,
                            key = ComposeHelper.getLazyKey(song, song.id, index)
                        ) { isDragging ->

                            QueueSongListItem(
                                song, index,
                                onPress = {
                                    player.seekTo(index, C.TIME_UNSET)
                                },
                                modifier = Modifier.draggableHandle(
                                    onDragStarted = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                    },
                                    onDragStopped = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                    },
                                ),
                            )


                        }
                    }
                }
            }
        }
    }
}