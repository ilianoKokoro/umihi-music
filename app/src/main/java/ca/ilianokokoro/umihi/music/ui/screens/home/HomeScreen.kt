package ca.ilianokokoro.umihi.music.ui.screens.home

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.ComposeHelper
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.ui.components.ErrorMessage
import ca.ilianokokoro.umihi.music.ui.components.FadingStatusBarWrapper
import ca.ilianokokoro.umihi.music.ui.components.LoadingAnimation
import ca.ilianokokoro.umihi.music.ui.components.dialog.PlaylistCreationDialog
import ca.ilianokokoro.umihi.music.ui.components.playlist.PlaylistCard

@Composable
fun HomeScreen(
    onSettingsButtonPress: () -> Unit,
    onPlaylistPressed: (playlistInfo: PlaylistInfo) -> Unit,
    application: Application,
    homeViewModel: HomeViewModel = viewModel(
        factory =
            HomeViewModel.Factory(application = application)
    )

) {
    val uiState = homeViewModel.uiState.collectAsStateWithLifecycle().value

    val lifecycleOwner = LocalLifecycleOwner.current
    var createPlaylistOpen by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            val loggedOut = uiState.screenState is ScreenState.LoggedOut
            val noPlaylistsFound =
                uiState.screenState is ScreenState.LoggedIn && uiState.screenState.playlistInfos.isEmpty()

            if (event == Lifecycle.Event.ON_RESUME && (loggedOut || noPlaylistsFound)) {
                homeViewModel.getPlaylists()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    FadingStatusBarWrapper {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        createPlaylistOpen = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = Icons.Rounded.Add.name
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                when (uiState.screenState) {
                    is ScreenState.LoggedIn -> {
                        val playlists = uiState.screenState.playlistInfos

                        if (playlists.isEmpty()) {
                            Text(
                                stringResource(R.string.no_playlists),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            PullToRefreshBox(
                                isRefreshing = uiState.isRefreshing,
                                onRefresh = homeViewModel::refreshPlaylists
                            ) {
                                LazyVerticalGrid(
                                    modifier = Modifier.fillMaxSize(),
                                    columns = GridCells.Adaptive(minSize = 150.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(
                                        top = paddingValues.calculateTopPadding(),
                                        bottom = Constants.Ui.SCROLLABLE_BOTTOM_PADDING
                                    )

                                ) {
                                    itemsIndexed(
                                        items = playlists,
                                        key = { index, playlist ->
                                            ComposeHelper.getLazyKey(
                                                playlist,
                                                playlist.id,
                                                index
                                            )
                                        }
                                    ) { _, playlist ->
                                        PlaylistCard(
                                            playlistInfo = playlist,
                                            onClicked = { onPlaylistPressed(playlist) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    ScreenState.LoggedOut -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.log_in_message),
                            textAlign = TextAlign.Center
                        )
                        FilledTonalButton(
                            onClick = onSettingsButtonPress,
                            shapes = ButtonDefaults.shapes()
                        )
                        {
                            Text(stringResource(R.string.open_settings))
                        }
                    }

                    ScreenState.Loading -> LoadingAnimation()
                    is ScreenState.Error -> ErrorMessage(
                        ex = uiState.screenState.exception,
                        onRetry = homeViewModel::getPlaylists
                    )

                }
                if (createPlaylistOpen) {
                    PlaylistCreationDialog(
                        onClose = { createPlaylistOpen = false },
                        onConfirm = { title, description, privacy ->
                            homeViewModel.createPlaylist(title, description, privacy)
                            createPlaylistOpen = false
                        })

                }
            }
        }
    }

}
