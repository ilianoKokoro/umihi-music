package ca.ilianokokoro.umihi.music.ui.screens.auth

import android.app.Application
import android.util.Log
import android.webkit.CookieManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SettingsState())
    val uiState = _uiState.asStateFlow()
    private val datastoreRepository = DatastoreRepository(application)

    fun onPageFinished(url: String?) {
        if (url?.contains(Constants.Auth.END_URL) == true && !_uiState.value.isLoggedIn) {
            val cookies = CookieManager.getInstance().getCookie(url).orEmpty()
            saveCookies(cookies)
            _uiState.update { it.copy(isLoggedIn = true) }
        }
    }

    private fun saveCookies(cookies: String) {
        Log.d("CustomLog", "Got cookies: $cookies")
        viewModelScope.launch {
            datastoreRepository.saveCookies(cookies)
        }
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AuthViewModel(application)
            }
        }
    }

}