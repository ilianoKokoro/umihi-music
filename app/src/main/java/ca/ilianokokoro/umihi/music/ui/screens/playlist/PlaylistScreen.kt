@file:OptIn(ExperimentalMaterial3Api::class)

package ca.ilianokokoro.umihi.music.ui.screens.playlist

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.ui.components.ErrorMessage
import ca.ilianokokoro.umihi.music.ui.components.LoadingAnimation
import ca.ilianokokoro.umihi.music.ui.components.PlaylistInfo
import ca.ilianokokoro.umihi.music.ui.components.SongListItem
import ca.ilianokokoro.umihi.music.ui.screens.playlist.components.ActionButtons

@Composable
fun PlaylistScreen(
    playlist: Playlist,
    onOpenPlayer: () -> Unit,
    onBack: () -> Unit,
    player: Player,
    modifier: Modifier = Modifier,
    application: Application,
    playlistViewModel: PlaylistViewModel = viewModel(
        factory =
            PlaylistViewModel.Factory(
                playlist = playlist,
                player = player,
                application = application
            )
    )

) {
    val uiState = playlistViewModel.uiState.collectAsStateWithLifecycle().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(playlist.title)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back_description)
                        )
                    }
                },
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            when (uiState.screenState) {

                is ScreenState.Success -> PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = playlistViewModel::refreshPlaylistInfo,
                    modifier = modifier
                        .fillMaxSize()
                ) {

                    LazyColumn(
                        modifier = modifier
                            .fillMaxSize()
                    ) {
                        val fullPlaylist = uiState.screenState.playlist
                        val songs = fullPlaylist.songs
                        item {
                            Column(modifier = modifier.padding(horizontal = 16.dp)) {
                                PlaylistInfo(
                                    playlist = fullPlaylist,
                                    onDownloadPressed = playlistViewModel::downloadPlaylist
                                )
                                ActionButtons(
                                    onPlayClicked = {
                                        onOpenPlayer()
                                        playlistViewModel.playPlaylist()
                                    },
                                    onShuffleClicked = {
                                        onOpenPlayer()
                                        playlistViewModel.shufflePlaylist()
                                    }
                                )
                            }
                        }


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
                            items(items = songs, key = {
                                it.id
                            }) { song ->
                                SongListItem(song, onPress = {
                                    onOpenPlayer()
                                    playlistViewModel.playPlaylist(song)
                                })
                            }
                        }

                    }
                }

                ScreenState.Loading -> LoadingAnimation()
                is ScreenState.Error -> ErrorMessage(
                    ex = uiState.screenState.exception,
                    onRetry = playlistViewModel::getPlaylistInfo
                )


            }

        }

    }

}