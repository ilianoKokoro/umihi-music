@file:OptIn(ExperimentalMaterial3Api::class)

package ca.ilianokokoro.umihi.music.ui.screens.player

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.media3.common.Player
import ca.ilianokokoro.umihi.music.R

@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    player: Player,
    modifier: Modifier = Modifier,
    application: Application,
    playlistViewModel: PlayerViewModel = viewModel(
        factory =
            PlayerViewModel.Factory(player = player, application = application)
    )

) {
    val uiState = playlistViewModel.uiState.collectAsStateWithLifecycle().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
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
                .padding(8.dp)
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                uiState.currentSong.title
            )
            Text(
                uiState.currentSong.artist
            )
            Text(
                uiState.progressMs.toString()
            )
            Text(
                uiState.isPlaying.toString()
            )
        }


    }
}
