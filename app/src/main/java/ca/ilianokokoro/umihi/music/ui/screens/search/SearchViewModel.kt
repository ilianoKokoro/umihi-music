package ca.ilianokokoro.umihi.music.ui.screens.search


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.data.repositories.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SearchState())
    val uiState = _uiState.asStateFlow()

    val songRepository = SongRepository()


    fun search() {
        viewModelScope.launch {
            songRepository.search(uiState.value.search).collect { apiResult ->
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

    fun onSearchFieldChange(newValue: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    search = newValue
                )
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