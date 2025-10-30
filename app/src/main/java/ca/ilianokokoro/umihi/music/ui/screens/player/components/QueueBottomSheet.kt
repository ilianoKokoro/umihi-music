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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import ca.ilianokokoro.umihi.music.extensions.getCurrentSong
import ca.ilianokokoro.umihi.music.extensions.getQueue
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

    var mutableSongList by remember { mutableStateOf(songs) }
    var lastDraggedSong by remember { mutableStateOf(Song("")) }
    var startIndex by remember { mutableIntStateOf(0) }

    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        mutableSongList = mutableSongList.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }

        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
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
                if (mutableSongList.isEmpty()) {
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
                        items = mutableSongList, key = { index, song -> song.id }
                    ) { index, song ->
                        ReorderableItem(
                            reorderableLazyListState,
                            key = song.id
                        ) { isDragging ->
                            QueueSongListItem(
                                song = song,
                                isCurrentSong = player.getCurrentSong().id == song.id,
                                onPress = {
                                    player.seekTo(index, C.TIME_UNSET)
                                },
                                scope = this,
                                onDragStarted =
                                    {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                        startIndex = index
                                        lastDraggedSong = song
                                    },
                                onDragStopped = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                    player.moveMediaItem(
                                        startIndex,
                                        mutableSongList.indexOf(mutableSongList.find { it.id == lastDraggedSong.id })
                                    )

                                    Log.d(
                                        "CustomLog",
                                        "Moved $startIndex to ${
                                            mutableSongList.indexOf(
                                                lastDraggedSong
                                            )
                                        }"
                                    )
                                    Log.d(
                                        "CustomLog",
                                        player.getQueue().map { "${it.title}\n" }.toString()
                                    )
                                    Log.d(
                                        "CustomLog",
                                        "Current new index : ${player.currentMediaItemIndex}"
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}