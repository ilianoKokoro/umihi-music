package ca.ilianokokoro.umihi.music.ui.screens.auth

import android.app.Application
import android.webkit.CookieManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printd
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import ca.ilianokokoro.umihi.music.models.Cookies
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SettingsState())
    //  val uiState = _uiState.asStateFlow()

    private val _eventsChannel = Channel<ScreenEvent.Out>()
    val eventChannel = _eventsChannel.consumeAsFlow()
    private val datastoreRepository = DatastoreRepository(application)

    fun onPageFinished(url: String?) {
        viewModelScope.launch {
            if (url?.contains(Constants.Auth.END_URL) == true && !_uiState.value.isLoggedIn) {
                val cookies = CookieManager.getInstance().getCookie(url).orEmpty()
                saveCookies(Cookies(cookies))
                _uiState.update { it.copy(isLoggedIn = true) }
                _eventsChannel.send(ScreenEvent.Out.LoginCompleted)
            }
        }
    }

    fun onDataSyncIdFound(dataSyncId: String) {
        viewModelScope.launch {
            datastoreRepository.saveDataSyncId(dataSyncId)
        }
    }

    private fun saveCookies(cookies: Cookies) {
        printd("Got cookies: $cookies")
        viewModelScope.launch {
            datastoreRepository.saveCookies(cookies)
        }
    }

    sealed interface ScreenEvent {
        sealed class Out {
            data object LoginCompleted : Out()
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