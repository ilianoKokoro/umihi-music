@file:OptIn(ExperimentalMaterial3Api::class)

package ca.ilianokokoro.umihi.music.ui.screens.playlists

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.ilianokokoro.umihi.music.R

@Composable
fun PlaylistsScreen(
    onSettingsButtonPress: () -> Unit,
    playlistsViewModel: PlaylistsViewModel = viewModel()
) {
    val uiState = playlistsViewModel.uiState.collectAsStateWithLifecycle().value


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.playlists))
                }, actions = {
                    IconButton(onClick = { onSettingsButtonPress() }) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.back_description)
                        )
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.screenState) {
                is ScreenState.Error -> TODO()
                ScreenState.Loading -> TODO()
                is ScreenState.Success -> {
                    val playlists = uiState.screenState.playlists

                    if (playlists.isEmpty()) {
                        Text(stringResource(R.string.no_playlists))
                    } else {
                        Text("yes playlists")
                    }
                }
            }


        }
    }
}