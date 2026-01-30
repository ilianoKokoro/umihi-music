@file:OptIn(ExperimentalMaterial3Api::class)

package ca.ilianokokoro.umihi.music.ui.screens.playlist

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.extensions.addNext
import ca.ilianokokoro.umihi.music.extensions.addToQueue
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.ui.components.ErrorMessage
import ca.ilianokokoro.umihi.music.ui.components.LoadingAnimation
import ca.ilianokokoro.umihi.music.ui.components.song.SongListItem
import ca.ilianokokoro.umihi.music.ui.screens.playlist.components.PlaylistHeader

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlaylistScreen(
    playlistInfo: PlaylistInfo,
    onOpenPlayer: () -> Unit,
    onBack: () -> Unit,
    player: Player,
    modifier: Modifier = Modifier,
    application: Application,
    playlistViewModel: PlaylistViewModel = viewModel(
        factory =
            PlaylistViewModel.Factory(
                playlistInfo = playlistInfo,
                player = player,
                application = application
            )
    )

) {
    val uiState = playlistViewModel.uiState.collectAsStateWithLifecycle().value


    Column(
        modifier = modifier
            .fillMaxSize()
    ) {

        if (uiState.screenState is ScreenState.Error) {
            ErrorMessage(
                ex = uiState.screenState.exception,
                onRetry = playlistViewModel::getPlaylistInfo
            )
        } else {
            val playlistInfo: Playlist = when (uiState.screenState) {
                is ScreenState.Loading -> {
                    Playlist(uiState.screenState.playlistInfo)
                }

                is ScreenState.Success -> {
                    uiState.screenState.playlist
                }
            }
            val songs = playlistInfo.songs

            if (uiState.screenState is ScreenState.Loading || songs.isEmpty()) {
                PlaylistHeader(
                    onOpenPlayer = onOpenPlayer,
                    isDownloading = uiState.isDownloading,
                    onDownloadPlaylist = playlistViewModel::downloadPlaylist,
                    onShufflePlaylist = playlistViewModel::shufflePlaylist,
                    onPlayPlaylist = playlistViewModel::playPlaylist,
                    playlist = playlistInfo
                )

                if (uiState.screenState is ScreenState.Loading) {
                    LoadingAnimation()
                } else {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = modifier
                            .fillMaxSize()
                    ) {
                        Text(
                            stringResource(R.string.empty_playlist),
                            textAlign = TextAlign.Center,
                        )
                    }
                }

            } else {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = playlistViewModel::refreshPlaylistInfo,
                    modifier = modifier
                        .fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = modifier
                            .fillMaxSize(),

                        ) {

                        item {
                            PlaylistHeader(
                                onOpenPlayer = onOpenPlayer,
                                isDownloading = uiState.isDownloading,
                                onDownloadPlaylist = playlistViewModel::downloadPlaylist,
                                onShufflePlaylist = playlistViewModel::shufflePlaylist,
                                onPlayPlaylist = playlistViewModel::playPlaylist,
                                playlist = playlistInfo
                            )
                        }

                        items(
                            items = songs,
                            key = { song ->
                                song.uid
                            }
                        ) { song ->
                            SongListItem(song, onPress = {
                                onOpenPlayer()
                                playlistViewModel.playPlaylist(song)
                            }, playNext = {
                                player.addNext(song, application)
                            }, addToQueue = {
                                player.addToQueue(song, application)
                            }, download = {
                                playlistViewModel.downloadSong(song)
                            })
                        }
                    }
                }
            }
        }


    }


}