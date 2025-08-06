package ca.ilianokokoro.umihi.music.ui.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel() : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsState())
    val uiState = _uiState.asStateFlow()

    init {
        Log.d("CustomLog", "init SettingsViewModel")
        getLoginState()
    }


    fun logIn(openAuthScreen: () -> Unit) {
        // TODO
        openAuthScreen()


    }

    fun logOut() {
        // TODO
        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(
                    screenState = ScreenState.Success(isLoggedIn = false)
                )
            }

        }
    }

    fun getLoginState() {
        // TODO
        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(
                    screenState = ScreenState.Success(isLoggedIn = false)
                )
            }
        }
    }


    fun clearDownloads() {
        // TODO
    }
}