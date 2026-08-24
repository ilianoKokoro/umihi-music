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
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.ExoCache
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper
import ca.ilianokokoro.umihi.music.core.managers.PlayerManager
import ca.ilianokokoro.umihi.music.core.managers.ScreenAwakeManager
import ca.ilianokokoro.umihi.music.core.managers.VersionManager
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import ca.ilianokokoro.umihi.music.data.repositories.DownloadRepository
import ca.ilianokokoro.umihi.music.ui.screens.settings.CacheType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SettingsState())
    val uiState = _uiState.asStateFlow()


    private val _application = application
    private val datastoreRepository = DatastoreRepository(application)
    private val downloadRepository = DownloadRepository(application)

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

    fun updateShowCountrySelectDialog(value: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(
                    showCountrySelectDialog = value
                )
            }
        }
    }

    fun updateCountryCode(countryCode: String) {
        updateSetting(DatastoreRepository.PreferenceKeys.COUNTRY_CODE, countryCode)
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

    fun updateShowCacheSizeInputDialog(show: Boolean, cacheType: CacheType = CacheType.AUDIO) {
        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(
                    showCacheSizeInputDialog = show,
                    cacheTypeForInput = cacheType
                )
            }
        }
    }

    fun updateShowAudioCacheClearConfirm(value: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(
                    showAudioCacheClearConfirm = value
                )
            }
        }
    }

    fun updateShowThumbnailCacheClearConfirm(value: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(
                    showThumbnailCacheClearConfirm = value
                )
            }
        }
    }

    fun saveCacheSize(sizeMB: Int, cacheType: CacheType) {
        viewModelScope.launch {
            val (key, min, max) = when (cacheType) {
                CacheType.AUDIO -> Triple(
                    DatastoreRepository.PreferenceKeys.EXOPLAYER_CACHE_SIZE,
                    Constants.ExoPlayer.Cache.MIN_SIZE_MB.toInt(),
                    Constants.ExoPlayer.Cache.MAX_SIZE_MB.toInt()
                )
                CacheType.THUMBNAIL -> Triple(
                    DatastoreRepository.PreferenceKeys.THUMBNAIL_CACHE_SIZE,
                    Constants.ExoPlayer.ThumbnailCache.MIN_SIZE_MB.toInt(),
                    Constants.ExoPlayer.ThumbnailCache.MAX_SIZE_MB.toInt()
                )
            }
            
            if (sizeMB in min..max) {
                datastoreRepository.save(key, sizeMB)
                Toast.makeText(
                    _application,
                    _application.getString(R.string.cache_saved),
                    Toast.LENGTH_SHORT
                ).show()
                getSettings()
            } else {
                Toast.makeText(
                    _application,
                    _application.getString(R.string.invalid_cache_size, min, max),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun clearAudioCache() {
        viewModelScope.launch {
            ExoCache(_application).clear()
            Toast.makeText(
                _application,
                _application.getString(R.string.audio_cache_cleared),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun clearThumbnailCache() {
        viewModelScope.launch {
            // Clear thumbnail cache directory
            val thumbnailCacheDir = File(_application.cacheDir, Constants.Downloads.THUMBNAILS_FOLDER)
            thumbnailCacheDir.deleteRecursively()
            Toast.makeText(
                _application,
                _application.getString(R.string.thumbnail_cache_cleared),
                Toast.LENGTH_LONG
            ).show()
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

    fun <T> updateSetting(key: Preferences.Key<T>, value: T) {
        viewModelScope.launch {
            datastoreRepository.save(
                key,
                value
            )
            getSettings()
        }
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(application)
            }
        }
    }
}