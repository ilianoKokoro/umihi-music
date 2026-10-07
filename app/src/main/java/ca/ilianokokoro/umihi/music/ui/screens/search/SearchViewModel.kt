package ca.ilianokokoro.umihi.music.ui.screens.search


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import ca.ilianokokoro.umihi.music.data.repositories.DownloadRepository
import ca.ilianokokoro.umihi.music.data.repositories.SongRepository
import ca.ilianokokoro.umihi.music.models.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SearchState())
    val uiState = _uiState.asStateFlow()

    private val datastoreRepository = DatastoreRepository(application)
    val songRepository = SongRepository(application)
    private val downloadRepository = DownloadRepository(application)

    init {
        observeLoginState()
    }

    private fun observeLoginState() {
        viewModelScope.launch {
            datastoreRepository.cookies.collect { cookies ->
                _uiState.update { it.copy(isLoggedIn = cookies.isNotEmpty()) }
            }
        }
    }


    fun search() {
        viewModelScope.launch {
            if (_uiState.value.search.isBlank()) {
                _uiState.update {
                    _uiState.value.copy(
                        screenState =
                            ScreenState.Success(results = listOf())
                    )
                }
                return@launch
            }

            songRepository.search(_uiState.value.search).collect { apiResult ->
                _uiState.update {
                    _uiState.value.copy(
                        screenState = when (apiResult) {
                            ApiResult.Loading -> ScreenState.Loading
                            is ApiResult.Error -> ScreenState.Error(apiResult.exception)
                            is ApiResult.Success -> {
                                ScreenState.Success(results = apiResult.data)
                            }
                        }
                    )
                }
            }
        }

    }


    fun downloadSong(song: Song) {
        viewModelScope.launch {
            val settings = datastoreRepository.getSettings()
            downloadRepository.downloadSong(song, settings.downloadOnMetered)
        }
    }

    fun onSearchFieldChange(newValue: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(search = newValue)
            }

            songRepository.searchAutocomplete(newValue).collect { apiResult ->
                _uiState.update {
                    _uiState.value.copy(
                        suggestions = when (apiResult) {
                            is ApiResult.Success -> {
                                apiResult.data
                            }

                            is ApiResult.Error -> listOf()
                            ApiResult.Loading -> _uiState.value.suggestions
                        }
                    )
                }
            }
        }
    }


    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SearchViewModel(application)
            }
        }
    }
}