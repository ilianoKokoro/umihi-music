package ca.ilianokokoro.umihi.music.ui.screens.player.components

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
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.components.song.QueueSongListItem
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueBottomSheet(
    changeVisibility: (visible: Boolean) -> Unit,
    currentSongIndex: Int,
    player: Player,
    songs: List<Song>,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current

    var mutableSongList by remember { mutableStateOf(songs) }
    var startIndex by remember { mutableIntStateOf(-1) }

    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        mutableSongList = mutableSongList.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }

        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    LaunchedEffect(null) {
        this.launch {
            lazyListState.animateScrollToItem(index = currentSongIndex)
        }
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
                        items = mutableSongList, key = { _, song -> song.uid }
                    ) { index, song ->
                        ReorderableItem(
                            reorderableLazyListState,
                            key = song.uid
                        ) { isDragging ->
                            QueueSongListItem(
                                song = song,
                                isCurrentSong = isDragging || (startIndex == -1 && mutableSongList.elementAt(
                                    currentSongIndex // TODO : Maybe change when the uuid is set to keep the current hightlighted song
                                ).uid == song.uid),
                                onPress = {
                                    player.seekTo(index, C.TIME_UNSET)
                                },
                                scope = this,
                                onDragStarted =
                                    {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                        startIndex =
                                            mutableSongList.indexOf(mutableSongList.find { it.uid == song.uid })
//
//                                        Log.d(
//                                            "CustomLog",
//                                            "Starting drag of ${song.title} from $startIndex"
//                                        )
                                    },
                                onDragStopped = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)

                                    val endIndex =
                                        mutableSongList.indexOf(mutableSongList.find { it.uid == song.uid })


                                    player.moveMediaItem(
                                        startIndex,
                                        endIndex
                                    )

                                    startIndex = -1

//                                    Log.d(
//                                        "CustomLog",
//                                        "Moved song ${song.title} from $startIndex to $endIndex"
//                                    )
//                                    Log.d(
//                                        "CustomLog",
//                                        player.getQueue().map { "${it.title}\n" }.toString()
//                                    )
//                                    Log.d(
//                                        "CustomLog",
//                                        "Current new index : ${player.currentMediaItemIndex}"
//                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}