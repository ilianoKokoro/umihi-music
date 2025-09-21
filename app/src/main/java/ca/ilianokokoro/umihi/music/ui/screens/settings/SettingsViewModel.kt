package ca.ilianokokoro.umihi.music.ui.screens.settings

import android.app.Application
import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.util.UnstableApi
import ca.ilianokokoro.umihi.music.core.ExoCache
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
    private val localSongRepository = AppDatabase.getInstance(application).songRepository()
    fun logOut() {
        viewModelScope.launch {
            datastoreRepository.saveCookies(Cookies(""))
            getLoginState()
        }
    }

    fun getLoginState() {
        viewModelScope.launch {
            val savedCookies = datastoreRepository.getCookies()
            _uiState.update {
                _uiState.value.copy(
                    screenState = ScreenState.Success(isLoggedIn = savedCookies.toRawCookie() != String())
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
        }
    }


    @OptIn(UnstableApi::class)
    fun clearDownloads() {
        viewModelScope.launch {
            localSongRepository.deleteAll()
            ExoCache(_application).clear()
            // TODO : Delete files
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