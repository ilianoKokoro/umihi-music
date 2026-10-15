package ca.ilianokokoro.umihi.music.ui.components.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.managers.PlayerManager
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.components.SheetHeader
import ca.ilianokokoro.umihi.music.ui.components.song.QueueSongListItem
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueBottomSheet(
    changeVisibility: (visible: Boolean) -> Unit,
    songs: List<Song>,
    currentIndex: Int,
    isMixActive: Boolean,
    isMixLoading: Boolean,
    isMixLoadingMore: Boolean,
    onToggleMix: () -> Unit,
    onStartMix: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current

    val normalSongs = songs.filterNot { it.isMix }
    val mixSongs = songs.filter { it.isMix }

    var mutableSongList by remember(normalSongs) { mutableStateOf(normalSongs) }
    var mutableMixList by remember(mixSongs) { mutableStateOf(mixSongs) }
    var startIndex by remember { mutableIntStateOf(0) }
    var mixStartIndex by remember { mutableIntStateOf(0) }

    fun globalIndex(song: Song): Int = songs.indexOfFirst { it.uid == song.uid }

    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val normalCount = mutableSongList.size
        val mixSectionStart = normalCount + 1
        val fromNormal = from.index < normalCount
        val toNormal = to.index < normalCount
        val fromMix = from.index >= mixSectionStart
        val toMix = to.index >= mixSectionStart

        if (fromNormal && toNormal) {
            val target = to.index.coerceIn(0, mutableSongList.lastIndex)
            mutableSongList = mutableSongList.toMutableList().apply {
                add(target, removeAt(from.index))
            }

            hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        } else if (fromMix && toMix && mutableMixList.isNotEmpty()) {
            val target = (to.index - mixSectionStart)
                .coerceIn(0, mutableMixList.lastIndex)
            mutableMixList = mutableMixList.toMutableList().apply {
                add(target, removeAt(from.index - mixSectionStart))
            }

            hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        }
    }

    LaunchedEffect(Unit) {
        this.launch {
            val indexToScroll =
                PlayerManager.currentController?.currentMediaItemIndex ?: return@launch

            lazyListState.animateScrollToItem(index = indexToScroll)
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            changeVisibility(false)
        },
        sheetState = rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            enabledValues = setOf(SheetValue.Expanded, SheetValue.Hidden),
        ),
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
        ) {
            SheetHeader(
                icon = Icons.AutoMirrored.Rounded.QueueMusic,
                title = stringResource(R.string.playing_now),
                modifier = modifier.padding(start = 8.dp, end = 8.dp, bottom = 12.dp),
            )

            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Radio,
                        contentDescription = null,
                    )
                },
                trailingContent = {
                    Switch(
                        checked = isMixActive,
                        enabled = !isMixLoading,
                        onCheckedChange = { onToggleMix() },
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .clip(Constants.Ui.SongItems.CORNER_RADIUS)
                    .clickable(enabled = !isMixLoading) { onToggleMix() },
                content = {
                    Text(stringResource(R.string.mix))
                },
            )

            if (isMixLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                ) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = stringResource(R.string.mix_loading),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                contentPadding = PaddingValues(
                    start = 8.dp,
                    top = 8.dp,
                    end = 8.dp,
                    bottom = Constants.Ui.SCROLLABLE_BOTTOM_PADDING
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (mutableSongList.isEmpty() && mixSongs.isEmpty() &&
                    !isMixLoading && !isMixLoadingMore
                ) {
                    item {
                        Text(
                            stringResource(R.string.queue_empty),
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
                        ) { _ ->
                            QueueSongListItem(
                                song = song,
                                isCurrentSong = song.uid == songs.getOrNull(currentIndex)?.uid,
                                onPress = {
                                    PlayerManager.seekToIndex(globalIndex(song))
                                },
                                scope = this,
                                onDragStarted = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                    startIndex = mutableSongList.indexOf(song)
                                },
                                onDelete = {
                                    mutableSongList = mutableSongList.toMutableList().apply {
                                        removeAt(index)
                                    }
                                    PlayerManager.removeMediaItem(globalIndex(song))
                                },
                                startMix = { onStartMix(song) },
                                onDragStopped = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                    PlayerManager.currentController?.moveMediaItem(
                                        startIndex,
                                        mutableSongList.indexOf(song)
                                    )
                                }
                            )
                        }
                    }

                    if (mixSongs.isNotEmpty()) {
                        item(key = "mix_header") {
                            Text(
                                text = stringResource(R.string.mix_up_next),
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                            )
                        }

                        items(
                            items = mutableMixList, key = { song -> song.uid }
                        ) { song ->
                            ReorderableItem(
                                reorderableLazyListState,
                                key = song.uid
                            ) { _ ->
                                QueueSongListItem(
                                    song = song,
                                    isCurrentSong = song.uid == songs.getOrNull(currentIndex)?.uid,
                                    onPress = {
                                        PlayerManager.seekToIndex(globalIndex(song))
                                    },
                                    scope = this,
                                    onDragStarted = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                        mixStartIndex = mutableMixList.indexOf(song)
                                    },
                                    onDelete = {
                                        mutableMixList = mutableMixList.toMutableList().apply {
                                            remove(song)
                                        }
                                        PlayerManager.removeMediaItem(globalIndex(song))
                                    },
                                    startMix = { onStartMix(song) },
                                    onDragStopped = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                        val mixGlobalStart = songs.indexOfFirst { it.isMix }
                                            .let { index -> if (index == -1) songs.size else index }
                                        PlayerManager.currentController?.moveMediaItem(
                                            mixGlobalStart + mixStartIndex,
                                            mixGlobalStart + mutableMixList.indexOf(song)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    if (isMixLoadingMore) {
                        item(key = "mix_loading_more") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                            ) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
