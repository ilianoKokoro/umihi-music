package ca.ilianokokoro.umihi.music.ui.screens.hide

import android.app.Application
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.ui.components.BackButton
import ca.ilianokokoro.umihi.music.ui.components.ErrorMessage
import ca.ilianokokoro.umihi.music.ui.components.FadingStatusBarWrapper
import ca.ilianokokoro.umihi.music.ui.components.LoadingAnimation
import ca.ilianokokoro.umihi.music.ui.components.playlist.PlaylistListItem
import ca.ilianokokoro.umihi.music.ui.navigation.viewmodels.SharedViewModel

@Composable
fun HideScreen(
    sharedViewModel: SharedViewModel,
    onPlaylistPressed: (PlaylistInfo) -> Unit,
    onBack: () -> Unit,
    application: Application,
    playlistViewModel: HideViewModel = viewModel(
        factory = HideViewModel.Factory(
            sharedViewModel = sharedViewModel,
            application = application
        )
    )
) {
    val uiState = playlistViewModel.uiState.collectAsStateWithLifecycle().value

    FadingStatusBarWrapper {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.hidden_playlists))
                    },
                    navigationIcon = {
                        BackButton(onBack = onBack)
                    }
                )
            }
        ) { paddingValues ->

            when (val state = uiState.screenState) {
                HideScreenState.Loading -> {
                    LoadingAnimation()
                }

                is HideScreenState.Error -> {
                    ErrorMessage(
                        ex = state.exception,
                        onRetry = playlistViewModel::getHiddenPlaylists
                    )
                }

                is HideScreenState.Success -> {
                    if (state.playlists.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_hidden_playlists),
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = paddingValues.calculateTopPadding(),
                                bottom = Constants.Ui.SCROLLABLE_BOTTOM_PADDING
                            )
                        ) {
                            items(
                                items = state.playlists,
                                key = { it.info.id }
                            ) { playlist ->
                                PlaylistListItem(
                                    playlist = playlist,
                                    onPlaylistPressed = onPlaylistPressed,
                                    onUnhidePlaylist = {
                                        playlistViewModel.unhidePlaylist(playlist.info)
                                    },
                                    onDeletePlaylist = {
                                        playlistViewModel.deletePlaylist(playlist.info)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}