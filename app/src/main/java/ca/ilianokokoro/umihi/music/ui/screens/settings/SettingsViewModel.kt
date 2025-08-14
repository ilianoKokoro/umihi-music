package ca.ilianokokoro.umihi.music.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import ca.ilianokokoro.umihi.music.models.Cookies
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SettingsState())
    val uiState = _uiState.asStateFlow()
    private val datastoreRepository = DatastoreRepository(application)

    fun logOut() {
        viewModelScope.launch {
            datastoreRepository.saveCookies(Cookies(""))
            getLoginState()
        }
    }

    fun getLoginState() {
        // Log.d("CustomLog", "getLoginState")
        viewModelScope.launch {
            val savedCookies = datastoreRepository.getCookies()
            _uiState.update {
                _uiState.value.copy(
                    screenState = ScreenState.Success(isLoggedIn = savedCookies.toRawCookie() != String())
                )
            }
        }
    }


    fun clearDownloads() {
        // TODO after downloads are implemented
    }


    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(application)
            }
        }
    }
}