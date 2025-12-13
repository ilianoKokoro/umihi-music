package ca.ilianokokoro.umihi.music.ui.screens.settings

import android.app.Application
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.util.UnstableApi
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.ExoCache
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper
import ca.ilianokokoro.umihi.music.core.managers.VersionManager
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import ca.ilianokokoro.umihi.music.models.Cookies
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SettingsState())
    val uiState = _uiState.asStateFlow()

    private val _application = application
    private val datastoreRepository = DatastoreRepository(application)
    fun logOut() {
        viewModelScope.launch {
            datastoreRepository.saveCookies(Cookies())
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


    @OptIn(UnstableApi::class)
    fun clearDownloads() {
        viewModelScope.launch {
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

    fun changeUpdateChannel(updateChannel: DatastoreRepository.UpdateChannel) {
        viewModelScope.launch {
            datastoreRepository.save(
                DatastoreRepository.PreferenceKeys.UPDATE_CHANNEL,
                updateChannel.toString()
            )
            getSettings()
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


    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(application)
            }
        }
    }
}