@file:OptIn(ExperimentalMaterial3Api::class)

package ca.ilianokokoro.umihi.music.ui.screens.settings

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.ui.components.ErrorMessage
import ca.ilianokokoro.umihi.music.ui.components.LoadingAnimation
import ca.ilianokokoro.umihi.music.ui.screens.settings.components.SettingsItem
import ca.ilianokokoro.umihi.music.ui.screens.settings.components.SettingsSection

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable // TODO : Extract strings
fun SettingsScreen(
    onBack: () -> Unit,
    openAuthScreen: () -> Unit,
    application: Application,
    settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(application))
) {
    val uiState = settingsViewModel.uiState.collectAsStateWithLifecycle().value

    // Refresh when returning to the screen
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                settingsViewModel.getLoginState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        shapes = IconButtonDefaults.shapes()
                    ) {
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
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                16.dp,
                alignment = Alignment.CenterVertically
            )

        ) {
            when (uiState.screenState) {
                is ScreenState.Success -> {

                    SettingsSection(
                        title = "Account"
                    ) {

                        if (uiState.screenState.isLoggedIn) {
                            SettingsItem(
                                title = "Log Out",
                                subtitle = "You are logged in to your YouTube account",
                                leadingIcon = Icons.AutoMirrored.Outlined.Logout,
                                onClick = settingsViewModel::logOut
                            )
                        } else {
                            SettingsItem(
                                title = "Log In",
                                subtitle = "You are currently not logged in",
                                leadingIcon = Icons.AutoMirrored.Outlined.Login,
                                onClick = openAuthScreen
                            )
                        }
                    }

                    SettingsSection(
                        title = "Data & Storage",
                    ) {

                        SettingsItem(
                            title = "Delete all downloads",
                            subtitle = "Deletes all the downloaded songs and cache",
                            leadingIcon = Icons.Outlined.Delete,
                            onClick = settingsViewModel::clearDownloads
                        )
//                                Spacer(modifier = Modifier.height(4.dp))
//                                SettingsItem(
//                                    title = "Delete app data",
//                                    subtitle = "Resets all the information",
//                                    leadingIcon = Icons.Outlined.Delete,
//                                    onClick = // Maybe ?
//                                )
                    }


                }

                ScreenState.Loading -> LoadingAnimation()
                is ScreenState.Error -> ErrorMessage(
                    ex = uiState.screenState.exception,
                    onRetry = settingsViewModel::getLoginState
                )
            }
        }

    }
}


