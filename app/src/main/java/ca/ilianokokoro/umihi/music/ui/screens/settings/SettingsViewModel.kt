package ca.ilianokokoro.umihi.music.ui.screens.settings

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.util.UnstableApi
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.CoilImageLoader
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.ExoCache
import ca.ilianokokoro.umihi.music.core.helpers.DownloadHelper
import ca.ilianokokoro.umihi.music.core.helpers.FileHelper
import ca.ilianokokoro.umihi.music.core.helpers.LogHelper.printe
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.folderSize
import ca.ilianokokoro.umihi.music.core.managers.PlayerManager
import ca.ilianokokoro.umihi.music.core.managers.ScreenAwakeManager
import ca.ilianokokoro.umihi.music.core.managers.VersionManager
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import ca.ilianokokoro.umihi.music.data.repositories.DownloadRepository
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.ui.navigation.viewmodels.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SettingsViewModel(
    private val sharedViewModel: SharedViewModel,
    application: Application
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SettingsState())
    val uiState = _uiState.asStateFlow()


    private val _application = application
    private val datastoreRepository = DatastoreRepository(application)
    private val downloadRepository = DownloadRepository(application)
    private val localPlaylistRepository =
        AppDatabase.getInstance(application).playlistRepository()

    fun logOut() {
        viewModelScope.launch {
            datastoreRepository.logOut()
            getSettings()
        }
    }

    fun getSettings() {
        viewModelScope.launch {
            val settings = datastoreRepository.getSettings()
            _uiState.update {
                _uiState.value.copy(
                    screenState = ScreenState.Success(settings = settings)
                )
            }
        }
        refreshStorageUsage()
    }

    @OptIn(UnstableApi::class)
    fun refreshStorageUsage() {
        viewModelScope.launch(Dispatchers.IO) {
            val thumbnailCacheDir =
                File(_application.cacheDir, Constants.Downloads.THUMBNAILS_FOLDER)
            val downloadLocation = datastoreRepository.getSettings().downloadLocation
            val audioCacheUsed = ExoCache.getInstance(_application).cache.cacheSpace
            val thumbnailCacheUsed = thumbnailCacheDir.folderSize()

            val downloadsUsage = DownloadsUsage(
                audioBytes = FileHelper.getDownloadFolderSize(_application, downloadLocation),
                imageBytes = 0L
            )

            _uiState.update {
                it.copy(
                    audioCacheUsed = audioCacheUsed,
                    thumbnailCacheUsed = thumbnailCacheUsed,
                    downloadsUsage = downloadsUsage
                )
            }
        }
    }

    fun updateShowLoginClearConfirm(value: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(
                    showLoginClearConfirm = value
                )
            }
        }
    }

    fun clearLogins() {
        viewModelScope.launch {
            WebStorage.getInstance().deleteAllData()
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
            logOut()
            Toast.makeText(
                _application,
                _application.getString(R.string.login_info_cleared),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun updateShowUpdateChannelSheet(value: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(
                    showUpdateChannelSheet = value
                )
            }
        }
    }

    fun updateShowDownloadDeleteConfirm(value: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(
                    showDownloadDeleteConfirm = value
                )
            }
        }
    }


    fun clearDownloads() {
        viewModelScope.launch {
            downloadRepository.cancelAllWorks()
            val downloadLocation = datastoreRepository.getSettings().downloadLocation

            FileHelper.clearDownloadFolder(_application, downloadLocation)
            AppDatabase.clearDownloads(_application)

            ExoCache.getInstance(_application).clear()
            CoilImageLoader.clear(_application)

            Toast.makeText(
                _application,
                _application.getString(R.string.downloads_cleared),
                Toast.LENGTH_LONG
            ).show()

            refreshStorageUsage()
        }
    }

    fun updateAudioOffloadSetting(value: Boolean) {
        PlayerManager.setAudioOffloadEnabled(value)
        updateSetting(
            DatastoreRepository.PreferenceKeys.USE_AUDIO_OFFLOAD,
            value
        )
    }

    fun updateKeepScreenOnSetting(value: Boolean) {
        ScreenAwakeManager.setKeepScreenOn(value)
        updateSetting(
            DatastoreRepository.PreferenceKeys.KEEP_SCREEN_ON,
            value
        )
    }

    fun updateOfflineModeSetting(value: Boolean) {
        viewModelScope.launch {
            datastoreRepository.save(
                DatastoreRepository.PreferenceKeys.OFFLINE_MODE,
                value
            )
            getSettings()
            sharedViewModel.requestPlaylistRefresh()
        }
    }


    fun checkForUpdates() {
        viewModelScope.launch {
            VersionManager.checkForUpdates(context = _application, manualCheck = true)
        }
    }

    fun isLoggedIn(): Boolean {
        val state = _uiState.value.screenState
        if (state !is ScreenState.Success) {
            return false
        }
        return !state.settings.cookies.isEmpty()
    }

    fun updateShowCacheSizeInputSheet(show: Boolean, cacheType: CacheType = CacheType.AUDIO) {
        _uiState.update {
            it.copy(
                showCacheSizeInputSheet = show,
                cacheTypeForInput = cacheType
            )
        }
    }

    fun updateShowCacheClearConfirm(show: Boolean) {
        _uiState.update { it.copy(showCacheClearConfirm = show) }
    }

    fun updateShowThemeSelectorSheet(show: Boolean) {
        _uiState.update { it.copy(showThemeSelectorSheet = show) }
    }

    fun saveCacheSize(sizeMB: Int, cacheType: CacheType) {
        viewModelScope.launch {
            when (cacheType) {
                CacheType.AUDIO -> {
                    updateSetting(
                        DatastoreRepository.PreferenceKeys.EXOPLAYER_CACHE_SIZE,
                        sizeMB
                    )
                }

                CacheType.THUMBNAIL -> {
                    updateSetting(
                        DatastoreRepository.PreferenceKeys.THUMBNAIL_CACHE_SIZE,
                        sizeMB
                    )
                }
            }
            updateShowCacheSizeInputSheet(false)
            refreshStorageUsage()
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            ExoCache.getInstance(_application).clear()
            withContext(Dispatchers.IO) {
                CoilImageLoader.clear(_application)
            }
            Toast.makeText(
                _application,
                _application.getString(R.string.cache_cleared),
                Toast.LENGTH_SHORT
            ).show()
            refreshStorageUsage()
        }
    }

    fun <T> updateSetting(key: Preferences.Key<T>, value: T) {
        viewModelScope.launch {
            datastoreRepository.save(
                key,
                value
            )
            getSettings()
        }
    }

    fun updateShowHiddenPlaylistsSheet(show: Boolean) {
        _uiState.update { it.copy(showHiddenPlaylistsSheet = show) }
    }

    fun updateShowDiagnosticsLogsSheet(show: Boolean) {
        _uiState.update { it.copy(showDiagnosticsLogsSheet = show) }
    }

    fun updateShowDownloadLocationDialog(show: Boolean) {
        _uiState.update { it.copy(showDownloadLocationDialog = show) }
    }

    fun onDownloadFolderPicked(uri: Uri?) {
        uri ?: return

        viewModelScope.launch {
            val oldLocation = datastoreRepository.getSettings().downloadLocation

            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION

            try {
                getApplication<Application>()
                    .contentResolver
                    .takePersistableUriPermission(uri, takeFlags)

                updateSetting(
                    DatastoreRepository.PreferenceKeys.DOWNLOAD_LOCATION,
                    uri.toString()
                )

                DownloadHelper.moveExistingDownloads(
                    context = _application,
                    oldLocation = oldLocation,
                    newLocation = uri
                )

                refreshStorageUsage()
            } catch (e: SecurityException) {
                printe(
                    message = "Failed to persist permission for SAF directory: $uri",
                    exception = e
                )
            }
        }
    }

    fun resetDownloadLocation() {
        viewModelScope.launch {
            val oldLocation = datastoreRepository.getSettings().downloadLocation

            updateSetting(DatastoreRepository.PreferenceKeys.DOWNLOAD_LOCATION, "")

            DownloadHelper.moveExistingDownloads(
                context = _application,
                oldLocation = oldLocation,
                newLocation = null
            )

            refreshStorageUsage()
        }
    }

    fun getHiddenPlaylists() {
        viewModelScope.launch {
            try {
                val playlists = localPlaylistRepository.fetchHiddenPlaylists()
                _uiState.update {
                    it.copy(
                        hiddenPlaylists = playlists
                    )
                }
            } catch (ex: Exception) {
                printe(message = ex.toString(), exception = ex)
                _uiState.update { it.copy(hiddenPlaylists = listOf()) }
            }
        }
    }

    fun unhidePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            localPlaylistRepository.insertPlaylist(
                playlist.info.copy(hidden = false)
            )
            sharedViewModel.requestPlaylistRefresh()
            getHiddenPlaylists()
        }
    }

    companion object {
        fun Factory(
            sharedViewModel: SharedViewModel,
            application: Application
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(sharedViewModel, application)
            }
        }
    }
}