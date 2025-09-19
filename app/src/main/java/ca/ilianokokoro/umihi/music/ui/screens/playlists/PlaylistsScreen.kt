@file:OptIn(ExperimentalMaterial3Api::class)

package ca.ilianokokoro.umihi.music.ui.screens.playlists

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import ca.ilianokokoro.umihi.music.core.helpers.ComposeHelper
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.ui.components.ErrorMessage
import ca.ilianokokoro.umihi.music.ui.components.LoadingAnimation
import ca.ilianokokoro.umihi.music.ui.components.PlaylistCard

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlaylistsScreen(
    onSettingsButtonPress: () -> Unit,
    onPlaylistPressed: (playlist: Playlist) -> Unit,
    application: Application,
    playlistsViewModel: PlaylistsViewModel = viewModel(
        factory =
            PlaylistsViewModel.Factory(application = application)
    )

) {
    val uiState = playlistsViewModel.uiState.collectAsStateWithLifecycle().value

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            val loggedOut = uiState.screenState is ScreenState.LoggedOut
            val noPlaylistsFound =
                uiState.screenState is ScreenState.LoggedIn && uiState.screenState.playlists.isEmpty()

            if (event == Lifecycle.Event.ON_RESUME && (loggedOut || noPlaylistsFound)) {
                playlistsViewModel.getPlaylists()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.playlists))
                }, actions = {
                    FilledIconButton(
                        onClick = onSettingsButtonPress,
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                    ) {

                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.back_description),

                            )
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            when (uiState.screenState) {
                is ScreenState.LoggedIn -> {
                    val playlists = uiState.screenState.playlists

                    if (playlists.isEmpty()) {
                        Text(
                            stringResource(R.string.no_playlists),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        PullToRefreshBox(
                            isRefreshing = uiState.isRefreshing,
                            onRefresh = playlistsViewModel::refreshPlaylists
                        ) {
                            LazyVerticalGrid(
                                modifier = Modifier.fillMaxSize(),
                                columns = GridCells.Adaptive(minSize = 150.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)

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
                                        playlist = playlist,
                                        onClicked = { onPlaylistPressed(playlist) }
                                    )
                                }
                            }
                        }
                    }
                }

                ScreenState.LoggedOut -> Text(
                    stringResource(R.string.log_in_message),
                    textAlign = TextAlign.Center
                )

                ScreenState.Loading -> LoadingAnimation()
                is ScreenState.Error -> ErrorMessage(
                    ex = uiState.screenState.exception,
                    onRetry = playlistsViewModel::getPlaylists
                )

            }
        }
    }
}
