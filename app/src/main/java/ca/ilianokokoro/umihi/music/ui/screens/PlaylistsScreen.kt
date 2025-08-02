@file:OptIn(ExperimentalMaterial3Api::class)

package ca.ilianokokoro.umihi.music.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ca.ilianokokoro.umihi.music.R

@Composable
fun PlaylistsScreen(onSettingsButtonPress: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.playlists))
                }
            )
        },

        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text("PlaylistsScreen")
            Button(onClick = onSettingsButtonPress) {
                Text("Open settings")
            }
        }
    }
}