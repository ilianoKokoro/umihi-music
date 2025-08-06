package ca.ilianokokoro.umihi.music.ui.screens.auth

import android.util.Log
import android.webkit.CookieManager
import androidx.lifecycle.ViewModel
import ca.ilianokokoro.umihi.music.core.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsState())
    val uiState = _uiState.asStateFlow()

    fun onPageFinished(url: String?) {
        if (url?.contains(Constants.Auth.END_URL) == true && !_uiState.value.isLoggedIn) {
            val cookies = CookieManager.getInstance().getCookie(url).orEmpty()
            saveCookies(cookies)
            _uiState.update { it.copy(isLoggedIn = true) }
        }
    }

    private fun saveCookies(cookies: String) {
        // TODO: Persist cookies securely (DataStore, EncryptedSharedPreferences, etc.)
        Log.d("CustomLog", "Got cookies: $cookies")
    }
}