package ca.ilianokokoro.umihi.music.ui.screens.search


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import ca.ilianokokoro.umihi.music.data.repositories.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SearchState())
    val uiState = _uiState.asStateFlow()

    private val songRepository = SongRepository()
    private val datastoreRepository = DatastoreRepository(application)

    fun search() {
        viewModelScope.launch {
            if (_uiState.value.search.isBlank()) {
                _uiState.update {
                    it.copy(
                        screenState = ScreenState.Success(results = listOf())
                    )
                }
                return@launch
            }

            val settings = datastoreRepository.getSettings()
            val currentFilter = _uiState.value.activeFilter

            songRepository.search(
                query = _uiState.value.search,
                filterParams = currentFilter.params,
                settings = settings
            ).collect { apiResult ->
                _uiState.update {
                    it.copy(
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
        _uiState.update {
            it.copy(search = newValue)
        }
    }

    fun onFilterChange(filter: SearchFilter) {
        if (_uiState.value.activeFilter != filter) {
            _uiState.update {
                it.copy(activeFilter = filter)
            }
            if (_uiState.value.search.isNotBlank()) {
                search()
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