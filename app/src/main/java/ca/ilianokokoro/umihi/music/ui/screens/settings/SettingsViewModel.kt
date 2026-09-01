package ca.ilianokokoro.umihi.music.ui.screens.settings

import android.app.Application
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
import ca.ilianokokoro.umihi.music.core.ExoCache
import ca.ilianokokoro.umihi.music.core.helpers.LogHelper.printe
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper
import ca.ilianokokoro.umihi.music.core.managers.PlayerManager
import ca.ilianokokoro.umihi.music.core.managers.ScreenAwakeManager
import ca.ilianokokoro.umihi.music.core.managers.VersionManager
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import ca.ilianokokoro.umihi.music.data.repositories.DownloadRepository
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.ui.navigation.viewmodels.SharedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    fun updateShowUpdateChannelDialog(value: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(
                    showUpdateChannelDialog = value
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


    @OptIn(UnstableApi::class)
    fun clearDownloads() {
        viewModelScope.launch {
            downloadRepository.cancelAllWorks()
            AppDatabase.clearDownloads(_application)
            ExoCache(_application).clear()
            UmihiHelper.getDownloadDirectory(context = _application)
                .deleteRecursively()
            Toast.makeText(
                _application,
                _application.getString(R.string.downloads_cleared),
                Toast.LENGTH_LONG
            ).show()
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

    fun updateShowCacheSizeInputDialog(show: Boolean, cacheType: CacheType = CacheType.AUDIO) {
        _uiState.update {
            it.copy(
                showCacheSizeInputDialog = show,
                cacheTypeForInput = cacheType
            )
        }
    }

    fun updateShowCacheClearConfirm(show: Boolean) {
        _uiState.update { it.copy(showCacheClearConfirm = show) }
    }
    
    fun saveCacheSize(sizeMB: Int, cacheType: CacheType) {
        viewModelScope.launch {
            when (cacheType) {
                CacheType.AUDIO -> updateSetting(
                    DatastoreRepository.PreferenceKeys.EXOPLAYER_CACHE_SIZE,
                    sizeMB
                )

                CacheType.THUMBNAIL -> {
                    updateSetting(
                        DatastoreRepository.PreferenceKeys.THUMBNAIL_CACHE_SIZE,
                        sizeMB
                    )
                    CoilImageLoader.reset(_application)
                }
            }
            updateShowCacheSizeInputDialog(false)
        }
    }

    @OptIn(UnstableApi::class)
    fun clearCache() {
        viewModelScope.launch {
            ExoCache(_application).clear()
            CoilImageLoader.clear(_application)
            Toast.makeText(
                _application,
                _application.getString(R.string.cache_cleared),
                Toast.LENGTH_SHORT
            ).show()
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