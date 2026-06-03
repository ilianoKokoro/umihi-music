@file:OptIn(ExperimentalMaterial3Api::class)

package ca.ilianokokoro.umihi.music.ui.screens.settings

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FeaturedPlayList
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.StayCurrentPortrait
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.managers.VersionManager
import ca.ilianokokoro.umihi.music.ui.components.ErrorMessage
import ca.ilianokokoro.umihi.music.ui.components.FadingStatusBarWrapper
import ca.ilianokokoro.umihi.music.ui.components.LoadingAnimation
import ca.ilianokokoro.umihi.music.ui.components.dialog.ConfirmDialog
import ca.ilianokokoro.umihi.music.ui.components.dialog.UpdateChannelDialog
import ca.ilianokokoro.umihi.music.ui.screens.settings.components.BooleanSettingItem
import ca.ilianokokoro.umihi.music.ui.screens.settings.components.SettingsItem
import ca.ilianokokoro.umihi.music.ui.screens.settings.components.SettingsSection


@Composable
fun SettingsScreen(
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
                settingsViewModel.getSettings()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    FadingStatusBarWrapper { statusBarHeight ->
        Scaffold(
            contentWindowInsets = WindowInsets(0.dp)
        ) { paddingValues ->
            when (val screenState = uiState.screenState) {
                ScreenState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingAnimation()
                    }
                }

                is ScreenState.Error -> {
                    ErrorMessage(
                        ex = screenState.exception,
                        onRetry = settingsViewModel::getSettings
                    )
                }

                is ScreenState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = statusBarHeight,
                                bottom = Constants.Ui.SCROLLABLE_BOTTOM_PADDING + paddingValues.calculateBottomPadding()
                            ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        SettingsSection(
                            title = stringResource(R.string.account)
                        ) {
                            if (settingsViewModel.isLoggedIn()) {
                                SettingsItem(
                                    title = stringResource(R.string.log_out),
                                    subtitle = stringResource(R.string.logged_in_message),
                                    leadingIcon = Icons.AutoMirrored.Outlined.Logout,
                                    onClick = settingsViewModel::logOut
                                )
                            } else {
                                SettingsItem(
                                    title = stringResource(R.string.log_in),
                                    subtitle = stringResource(R.string.logged_out_message),
                                    leadingIcon = Icons.AutoMirrored.Outlined.Login,
                                    onClick = openAuthScreen
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            SettingsItem(
                                title = stringResource(R.string.clear_login_info),
                                subtitle = stringResource(R.string.clear_login_message),
                                leadingIcon = Icons.Outlined.Delete,
                                onClick = settingsViewModel::clearLogins
                            )
                        }

                        SettingsSection(
                            title = stringResource(R.string.general)
                        ) {
                            BooleanSettingItem(
                                title = stringResource(R.string.show_podcast_playlist_title),
                                subtitle = stringResource(R.string.show_podcast_playlist_description),
                                leadingIcon = Icons.AutoMirrored.Outlined.FeaturedPlayList,
                                value = screenState.settings.showPodcastPlaylist,
                                onToggle = {
                                    settingsViewModel.updatePodcastPlaylistVisibility(it)
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            BooleanSettingItem(
                                title = stringResource(R.string.keep_screen_on_title),
                                subtitle = stringResource(R.string.keep_screen_on_title_description),
                                leadingIcon = Icons.Outlined.StayCurrentPortrait,
                                value = screenState.settings.keepScreenOn,
                                onToggle = {
                                    settingsViewModel.updateKeepScreenOnSetting(it)
                                }
                            )
                        }

                        SettingsSection(
                            title = stringResource(R.string.playback)
                        ) {
                            BooleanSettingItem(
                                title = stringResource(R.string.enable_audio_offload),
                                subtitle = stringResource(R.string.audio_offload_subtitle),
                                leadingIcon = Icons.Outlined.Memory,
                                value = screenState.settings.useAudioOffload,
                                onToggle = {
                                    settingsViewModel.updateAudioOffloadSetting(it)
                                }
                            )
                        }

                        SettingsSection(
                            title = stringResource(R.string.data_and_storage)
                        ) {
                            SettingsItem(
                                title = stringResource(R.string.delete_downloads),
                                subtitle = stringResource(R.string.clear_data_message),
                                leadingIcon = Icons.Outlined.Delete,
                                onClick = {
                                    settingsViewModel.updateShowDownloadDeleteConfirm(true)
                                }
                            )
                        }

                        SettingsSection(
                            title = stringResource(R.string.app_info)
                        ) {
                            SettingsItem(
                                title = stringResource(R.string.check_for_updates),
                                subtitle = stringResource(
                                    R.string.current_version,
                                    VersionManager.getVersionName()
                                ),
                                leadingIcon = Icons.Outlined.Update,
                                onClick = settingsViewModel::checkForUpdates
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            SettingsItem(
                                title = stringResource(R.string.change_update_channel),
                                subtitle = stringResource(
                                    R.string.current_update_channel_body,
                                    screenState.settings.updateChannel
                                ),
                                leadingIcon = Icons.Outlined.SystemUpdate,
                                onClick = {
                                    settingsViewModel.updateShowUpdateChannelDialog(true)
                                }
                            )
                        }

                        if (uiState.showUpdateChannelDialog) {
                            UpdateChannelDialog(
                                selectedOption = screenState.settings.updateChannel,
                                onChange = {
                                    settingsViewModel.changeUpdateChannel(it)
                                },
                                onClose = {
                                    settingsViewModel.updateShowUpdateChannelDialog(false)
                                }
                            )
                        } else if (uiState.showDownloadDeleteConfirm) {
                            ConfirmDialog(
                                title = stringResource(R.string.download_clear_confirm_title),
                                text = stringResource(R.string.download_clear_confirm_text),
                                onConfirm = {
                                    settingsViewModel.clearDownloads()
                                    settingsViewModel.updateShowDownloadDeleteConfirm(false)
                                },
                                onDismiss = {
                                    settingsViewModel.updateShowDownloadDeleteConfirm(false)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}