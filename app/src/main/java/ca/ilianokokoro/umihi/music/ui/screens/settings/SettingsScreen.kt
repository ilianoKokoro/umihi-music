package ca.ilianokokoro.umihi.music.ui.screens.settings

import android.app.Application
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.ilianokokoro.umihi.music.BuildConfig
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.DiagnosticLog
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.usedFraction
import ca.ilianokokoro.umihi.music.core.managers.VersionManager
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository.PreferenceKeys
import ca.ilianokokoro.umihi.music.extensions.folderDisplayPath
import ca.ilianokokoro.umihi.music.models.enums.ThemeMode
import ca.ilianokokoro.umihi.music.ui.components.ErrorMessage
import ca.ilianokokoro.umihi.music.ui.components.LoadingAnimation
import ca.ilianokokoro.umihi.music.ui.components.StatusBarWrapper
import ca.ilianokokoro.umihi.music.ui.components.bottomsheet.CacheSizeInputBottomSheet
import ca.ilianokokoro.umihi.music.ui.components.bottomsheet.DiagnosticsLogBottomSheet
import ca.ilianokokoro.umihi.music.ui.components.bottomsheet.HiddenPlaylistsBottomSheet
import ca.ilianokokoro.umihi.music.ui.components.bottomsheet.ThemeSelectorBottomSheet
import ca.ilianokokoro.umihi.music.ui.components.bottomsheet.UpdateChannelBottomSheet
import ca.ilianokokoro.umihi.music.ui.components.dialog.ConfirmDialog
import ca.ilianokokoro.umihi.music.ui.components.dialog.DownloadLocationDialog
import ca.ilianokokoro.umihi.music.ui.icons.autorenew
import ca.ilianokokoro.umihi.music.ui.icons.brightness_auto
import ca.ilianokokoro.umihi.music.ui.icons.cloud_download
import ca.ilianokokoro.umihi.music.ui.icons.cloud_off
import ca.ilianokokoro.umihi.music.ui.icons.dark_mode
import ca.ilianokokoro.umihi.music.ui.icons.delete
import ca.ilianokokoro.umihi.music.ui.icons.folder
import ca.ilianokokoro.umihi.music.ui.icons.history
import ca.ilianokokoro.umihi.music.ui.icons.image
import ca.ilianokokoro.umihi.music.ui.icons.info
import ca.ilianokokoro.umihi.music.ui.icons.light_mode
import ca.ilianokokoro.umihi.music.ui.icons.login
import ca.ilianokokoro.umihi.music.ui.icons.logout
import ca.ilianokokoro.umihi.music.ui.icons.memory
import ca.ilianokokoro.umihi.music.ui.icons.mobile
import ca.ilianokokoro.umihi.music.ui.icons.system_update
import ca.ilianokokoro.umihi.music.ui.icons.text_snippet
import ca.ilianokokoro.umihi.music.ui.icons.update
import ca.ilianokokoro.umihi.music.ui.icons.visibility
import ca.ilianokokoro.umihi.music.ui.navigation.viewmodels.SharedViewModel
import ca.ilianokokoro.umihi.music.ui.screens.settings.components.BooleanSettingItem
import ca.ilianokokoro.umihi.music.ui.screens.settings.components.SettingSpacer
import ca.ilianokokoro.umihi.music.ui.screens.settings.components.SettingsItem
import ca.ilianokokoro.umihi.music.ui.screens.settings.components.SettingsSection


@Composable
fun SettingsScreen(
    openAuthScreen: () -> Unit,
    application: Application,
    sharedViewModel: SharedViewModel,
    settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(sharedViewModel, application)
    )
) {
    val context = LocalContext.current
    val uiState = settingsViewModel.uiState.collectAsStateWithLifecycle().value

    // Refresh when returning to the screen
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                settingsViewModel.getSettings()
                settingsViewModel.getHiddenPlaylists()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri -> settingsViewModel.onDownloadFolderPicked(uri) }

    StatusBarWrapper { statusBarHeight ->
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
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )


                        SettingsSection(
                            title = stringResource(R.string.app_info)
                        ) {
                            SettingsItem(
                                title = stringResource(R.string.current_version),
                                subtitle = VersionManager.getVersionName(),
                                leadingIcon = info,
                                onClick = { }
                            )
                        }

                        if (BuildConfig.BUILD_TYPE == DiagnosticLog.DIAGNOSTIC_BUILD_TYPE) {
                            SettingsSection(
                                title = stringResource(R.string.diagnostics)
                            ) {
                                SettingsItem(
                                    title = stringResource(R.string.show_logs),
                                    subtitle = stringResource(R.string.view_the_recorded_diagnostics_logs),
                                    leadingIcon = text_snippet,
                                    onClick = {
                                        settingsViewModel.updateShowDiagnosticsLogsSheet(true)
                                    }
                                )
                            }
                        }


                        SettingsSection(
                            title = stringResource(R.string.account)
                        ) {
                            if (settingsViewModel.isLoggedIn()) {
                                SettingsItem(
                                    title = stringResource(R.string.log_out),
                                    subtitle = stringResource(R.string.logged_in_message),
                                    leadingIcon = logout,
                                    onClick = settingsViewModel::logOut
                                )
                            } else {
                                SettingsItem(
                                    title = stringResource(R.string.log_in),
                                    subtitle = stringResource(R.string.logged_out_message),
                                    leadingIcon = login,
                                    onClick = openAuthScreen
                                )
                            }
                            SettingSpacer()
                            SettingsItem(
                                title = stringResource(R.string.clear_login_info),
                                subtitle = stringResource(R.string.clear_login_message),
                                leadingIcon = delete,
                                onClick = {
                                    settingsViewModel.updateShowLoginClearConfirm(true)
                                }
                            )
                        }

                        SettingsSection(
                            title = stringResource(R.string.general)
                        ) {
                            SettingsItem(
                                title = stringResource(R.string.theme),
                                subtitle = stringResource(
                                    when (screenState.settings.themeMode) {
                                        ThemeMode.DARK -> R.string.theme_dark
                                        ThemeMode.LIGHT -> R.string.theme_light
                                        ThemeMode.SYSTEM -> R.string.theme_system
                                    }
                                ),
                                leadingIcon = when (screenState.settings.themeMode) {
                                    ThemeMode.DARK -> dark_mode
                                    ThemeMode.LIGHT -> light_mode
                                    ThemeMode.SYSTEM -> brightness_auto
                                },
                                onClick = {
                                    settingsViewModel.updateShowThemeSelectorSheet(true)
                                }
                            )
                            SettingSpacer()
                            SettingsItem(
                                title = stringResource(R.string.show_hidden_playlists_title),
                                subtitle = stringResource(R.string.show_hidden_playlists_description),
                                leadingIcon = visibility,
                                onClick = {
                                    settingsViewModel.updateShowHiddenPlaylistsSheet(true)
                                }
                            )
                            SettingSpacer()
                            BooleanSettingItem(
                                title = stringResource(R.string.keep_screen_on_title),
                                subtitle = stringResource(R.string.keep_screen_on_title_description),
                                leadingIcon = mobile,
                                value = screenState.settings.keepScreenOn,
                                onToggle = settingsViewModel::updateKeepScreenOnSetting
                            )
                            SettingSpacer()
                            BooleanSettingItem(
                                title = stringResource(R.string.send_playback_data_title),
                                subtitle = stringResource(R.string.send_playback_data_description),
                                leadingIcon = history,
                                value = screenState.settings.sendPlaybackData,
                                onToggle = {
                                    settingsViewModel.updateSetting(
                                        PreferenceKeys.SEND_PLAYBACK_DATA,
                                        it
                                    )
                                }
                            )
                        }

                        SettingsSection(
                            title = stringResource(R.string.playback)
                        ) {
                            BooleanSettingItem(
                                title = stringResource(R.string.enable_audio_offload),
                                subtitle = stringResource(R.string.audio_offload_subtitle),
                                leadingIcon = memory,
                                value = screenState.settings.useAudioOffload,
                                onToggle = settingsViewModel::updateAudioOffloadSetting
                            )
                        }

                        SettingsSection(
                            title = stringResource(R.string.data_and_storage)
                        ) {
                            BooleanSettingItem(
                                title = stringResource(R.string.offline_mode_title),
                                subtitle = stringResource(R.string.offline_mode_description),
                                leadingIcon = cloud_off,
                                value = screenState.settings.offlineMode,
                                onToggle = settingsViewModel::updateOfflineModeSetting
                            )
                            SettingSpacer()
                            BooleanSettingItem(
                                title = stringResource(R.string.download_on_metered_title),
                                subtitle = stringResource(R.string.download_on_metered_description),
                                leadingIcon = cloud_download,
                                value = screenState.settings.downloadOnMetered,
                                onToggle = {
                                    settingsViewModel.updateSetting(
                                        PreferenceKeys.DOWNLOAD_ON_METERED,
                                        it
                                    )
                                }
                            )
                            SettingSpacer()
                            val downloadLocation =
                                runCatching { screenState.settings.downloadLocation?.folderDisplayPath() }.getOrNull()
                                    ?: stringResource(R.string.internal)
                            SettingsItem(
                                title = stringResource(R.string.change_download_location),
                                subtitle = stringResource(
                                    R.string.current_location,
                                    downloadLocation
                                ),
                                leadingIcon = folder,
                                onClick = {
                                    settingsViewModel.updateShowDownloadLocationDialog(true)
                                }
                            )
                            SettingSpacer()
                            SettingsItem(
                                title = stringResource(R.string.delete_downloads),
                                subtitle = stringResource(
                                    R.string.downloads_audio_used,
                                    Formatter.formatShortFileSize(
                                        application,
                                        uiState.downloadsUsage.audioBytes
                                    )
                                ) + " ${stringResource(R.string.dot)} " + stringResource(
                                    R.string.downloads_images_used,
                                    Formatter.formatShortFileSize(
                                        application,
                                        uiState.downloadsUsage.imageBytes
                                    )
                                ),
                                leadingIcon = delete,
                                onClick = {
                                    settingsViewModel.updateShowDownloadDeleteConfirm(true)
                                }
                            )
                        }

                        SettingsSection(
                            title = stringResource(R.string.cache_management)
                        ) {
                            SettingsItem(
                                title = stringResource(R.string.exoplayer_cache_title),
                                subtitle = stringResource(
                                    R.string.cache_used_state,
                                    Formatter.formatShortFileSize(
                                        application,
                                        uiState.audioCacheUsed
                                    ),
                                    screenState.settings.exoPlayerCacheSizeMB
                                ),
                                leadingIcon = memory,
                                progress = usedFraction(
                                    usedBytes = uiState.audioCacheUsed,
                                    limitMB = screenState.settings.exoPlayerCacheSizeMB
                                ),
                                onClick = {
                                    settingsViewModel.updateShowCacheSizeInputSheet(
                                        true,
                                        CacheType.AUDIO
                                    )
                                }
                            )
                            SettingSpacer()

                            SettingsItem(
                                title = stringResource(R.string.thumbnail_cache_title),
                                subtitle = stringResource(
                                    R.string.cache_used_state,
                                    Formatter.formatShortFileSize(
                                        application,
                                        uiState.thumbnailCacheUsed
                                    ),
                                    screenState.settings.thumbnailCacheSizeMB
                                ),
                                leadingIcon = image,
                                progress = usedFraction(
                                    usedBytes = uiState.thumbnailCacheUsed,
                                    limitMB = screenState.settings.thumbnailCacheSizeMB
                                ),
                                onClick = {
                                    settingsViewModel.updateShowCacheSizeInputSheet(
                                        true,
                                        CacheType.THUMBNAIL
                                    )
                                }
                            )
                            SettingSpacer()
                            SettingsItem(
                                title = stringResource(R.string.clear_cache),
                                subtitle = stringResource(R.string.clear_cache_message),
                                leadingIcon = delete,
                                onClick = {
                                    settingsViewModel.updateShowCacheClearConfirm(true)
                                }
                            )
                        }

                        if (BuildConfig.UPDATER_ENABLED) {
                            SettingsSection(
                                title = stringResource(R.string.updates)
                            ) {
                                SettingsItem(
                                    title = stringResource(R.string.check_for_updates),
                                    subtitle = stringResource(R.string.check_update_setting_description),
                                    leadingIcon = update,
                                    onClick = settingsViewModel::checkForUpdates
                                )
                                SettingSpacer()
                                BooleanSettingItem(
                                    title = stringResource(R.string.auto_update_title),
                                    subtitle = stringResource(R.string.auto_update_subtitle),
                                    leadingIcon = autorenew,
                                    value = screenState.settings.updateChecking,
                                    onToggle = {
                                        settingsViewModel.updateSetting(
                                            PreferenceKeys.AUTO_UPDATE,
                                            it
                                        )
                                    }
                                )
                                SettingSpacer()

                                SettingsItem(
                                    title = stringResource(R.string.change_update_channel),
                                    subtitle = stringResource(
                                        R.string.current_update_channel_body,
                                        screenState.settings.updateChannel
                                    ),
                                    leadingIcon = system_update,
                                    onClick = {
                                        settingsViewModel.updateShowUpdateChannelSheet(true)
                                    }
                                )
                            }
                        }

                        if (uiState.showThemeSelectorSheet) {
                            ThemeSelectorBottomSheet(
                                selectedOption = screenState.settings.themeMode,
                                onChange = {
                                    settingsViewModel.updateSetting(
                                        PreferenceKeys.THEME_MODE,
                                        it.name
                                    )
                                },
                                onClose = {
                                    settingsViewModel.updateShowThemeSelectorSheet(false)
                                }
                            )
                        } else if (uiState.showUpdateChannelSheet) {
                            UpdateChannelBottomSheet(
                                selectedOption = screenState.settings.updateChannel,
                                onChange = {
                                    settingsViewModel.updateSetting(
                                        PreferenceKeys.UPDATE_CHANNEL,
                                        it.toString()
                                    )
                                },
                                onClose = {
                                    settingsViewModel.updateShowUpdateChannelSheet(false)
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
                        } else if (uiState.showCacheSizeInputSheet) {
                            val initialSize = when (uiState.cacheTypeForInput) {
                                CacheType.AUDIO -> screenState.settings.exoPlayerCacheSizeMB
                                CacheType.THUMBNAIL -> screenState.settings.thumbnailCacheSizeMB
                            }
                            CacheSizeInputBottomSheet(
                                cacheType = uiState.cacheTypeForInput,
                                initialSizeMB = initialSize,
                                onConfirm = { sizeMB ->
                                    settingsViewModel.saveCacheSize(
                                        sizeMB,
                                        uiState.cacheTypeForInput
                                    )
                                }
                            )
                        } else if (uiState.showCacheClearConfirm) {
                            ConfirmDialog(
                                title = stringResource(R.string.clear_cache),
                                text = stringResource(R.string.clear_cache_message),
                                onConfirm = {
                                    settingsViewModel.clearCache()
                                    settingsViewModel.updateShowCacheClearConfirm(false)
                                },
                                onDismiss = {
                                    settingsViewModel.updateShowCacheClearConfirm(false)
                                }
                            )
                        } else if (uiState.showLoginClearConfirm) {
                            ConfirmDialog(
                                title = stringResource(R.string.clear_login_info),
                                text = stringResource(R.string.clear_login_confirm_message),
                                onConfirm = {
                                    settingsViewModel.clearLogins()
                                    settingsViewModel.updateShowLoginClearConfirm(false)
                                },
                                onDismiss = {
                                    settingsViewModel.updateShowLoginClearConfirm(false)
                                }
                            )
                        } else if (uiState.showHiddenPlaylistsSheet) {
                            HiddenPlaylistsBottomSheet(
                                playlists = uiState.hiddenPlaylists,
                                onUnhidePlaylist = { settingsViewModel.unhidePlaylist(it) },
                                onDismiss = { settingsViewModel.updateShowHiddenPlaylistsSheet(false) }
                            )
                        } else if (uiState.showDiagnosticsLogsSheet) {
                            DiagnosticsLogBottomSheet(
                                onExport = { DiagnosticLog.share(application) },
                                onClear = { DiagnosticLog.clear() },
                                onDismiss = {
                                    settingsViewModel.updateShowDiagnosticsLogsSheet(false)
                                }
                            )
                        } else if (uiState.showDownloadLocationDialog) {
                            DownloadLocationDialog(
                                onConfirm = {
                                    folderPicker.launch(screenState.settings.downloadLocation)
                                    settingsViewModel.updateShowDownloadLocationDialog(false)
                                },
                                onReset = {
                                    settingsViewModel.resetDownloadLocation()
                                    settingsViewModel.updateShowDownloadLocationDialog(false)
                                },
                                onDismiss = {
                                    settingsViewModel.updateShowDownloadLocationDialog(false)
                                }
                            )
                        }
                    }
                }
            }
        }
    }


}
