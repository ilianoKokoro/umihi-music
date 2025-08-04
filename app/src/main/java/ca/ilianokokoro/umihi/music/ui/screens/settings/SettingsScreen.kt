@file:OptIn(ExperimentalMaterial3Api::class)

package ca.ilianokokoro.umihi.music.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.ilianokokoro.umihi.music.R

@Composable
fun SettingsScreen(onBack: () -> Unit, settingsViewModel: SettingsViewModel = viewModel()) {
    val uiState = settingsViewModel.uiState.collectAsStateWithLifecycle().value


    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back_description)
                        )
                    }
                },
                title = {
                    Text(stringResource(R.string.settings))
                }
            )
        }, modifier = Modifier
            .fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                16.dp,
                alignment = Alignment.CenterVertically
            )

        ) {
            when (uiState.screenState) {
                is ScreenState.Success -> {
                    if (uiState.screenState.isLoggedIn) {
                        SettingCard(
                            "You are logged in to your YouTube account",
                            "Log Out"
                        ) { settingsViewModel.logOut() }
                    } else {
                        SettingCard(
                            "You are currently not logged in",
                            "Log In"
                        ) { settingsViewModel.logIn() }
                    }

                    SettingCard(
                        "Press this button to remove all downloaded songs",
                        "Delete downloads"
                    ) { settingsViewModel.clearDownloads() }
                }

                ScreenState.Loading -> TODO()
                is ScreenState.Error -> TODO()
            }

        }
    }
}

@Composable
fun SettingCard(text: String, buttonText: String, onButtonPress: () -> Unit) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text)
            Button(onClick = onButtonPress) {
                Text(buttonText)
            }
        }
    }

}
