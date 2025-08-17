@file:OptIn(ExperimentalMaterial3Api::class)

package ca.ilianokokoro.umihi.music.ui.screens.player

import android.app.Application
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.ui.components.SquareImage
import ca.ilianokokoro.umihi.music.ui.screens.player.components.PlayerControls

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
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
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
                .padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Box(
                modifier = Modifier
                    .weight(3f)
                    .padding(horizontal = 20.dp)
            ) {
                SquareImage(url = uiState.currentSong.thumbnail)
            }

            // Song Info + Controls
            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = uiState.currentSong.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = modifier.basicMarquee()
                    )
                    Text(
                        text = uiState.currentSong.artist,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = modifier.basicMarquee()
                    )
                }

                PlayerControls(
                    isPlaying = uiState.isPlaying,
                    isLoading = uiState.isLoading,
                    position = uiState.progressMs,
                    duration = uiState.durationMs,
                    onPlay = playlistViewModel::play,
                    onPause = playlistViewModel::pause,
                    onSeek = playlistViewModel::seek,
                    onSeekPlayer = playlistViewModel::seekPlayer,
                    onSeekToNext = playlistViewModel::seekToNext,
                    onSeekToPrevious = playlistViewModel::seekToPrevious,
                    onUpdateSeekBarHeldState = playlistViewModel::updateSeekBarHeldState
                )
            }

            // Queue Button
            IconButton(
                onClick = {
                    // TODO
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                    contentDescription = "Show Queue"
                )
            }
        }
    }
}
