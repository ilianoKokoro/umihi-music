package ca.ilianokokoro.umihi.music.ui.components.bottomsheet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import ca.ilianokokoro.umihi.music.data.repositories.PlaylistRepository
import ca.ilianokokoro.umihi.music.models.AddToPlaylistOption
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.Privacy
import ca.ilianokokoro.umihi.music.models.Song
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class AddToPlaylistScreenState {
    data object Loading : AddToPlaylistScreenState()
    data class Success(val options: List<AddToPlaylistOption>) : AddToPlaylistScreenState()
    data class Error(val exception: Exception) : AddToPlaylistScreenState()
}

data class AddToPlaylistUiState(
    val screenState: AddToPlaylistScreenState = AddToPlaylistScreenState.Loading,
    val pendingToggles: Set<String> = emptySet(),
    val submitting: Boolean = false,
) {
    val hasPendingChanges: Boolean
        get() = pendingToggles.isNotEmpty()

    fun isChecked(option: AddToPlaylistOption): Boolean =
        option.playlistId in pendingToggles
}

class AddToPlaylistViewModel(
    private val application: Application,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(AddToPlaylistUiState())
    val uiState = _uiState.asStateFlow()

    private val playlistRepository = PlaylistRepository(application)
    private val datastoreRepository = DatastoreRepository(application)

    private var currentVideoId: String? = null

    fun load(videoId: String) {
        currentVideoId = videoId
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    screenState = AddToPlaylistScreenState.Loading,
                    pendingToggles = emptySet(),
                    submitting = false,
                )
            }
            try {
                val settings = datastoreRepository.getSettings()
                if (settings.cookies.isEmpty()) {
                    throw Exception(application.getString(R.string.failed_get_to_login_cookies))
                }

                playlistRepository.retrieveAddToPlaylistOptions(videoId, settings)
                    .collect { apiResult ->
                        _uiState.update { currentState ->
                            currentState.copy(
                                screenState = when (apiResult) {
                                    is ApiResult.Error -> AddToPlaylistScreenState.Error(apiResult.exception)
                                    ApiResult.Loading -> AddToPlaylistScreenState.Loading
                                    is ApiResult.Success -> AddToPlaylistScreenState.Success(apiResult.data)
                                }
                            )
                        }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(screenState = AddToPlaylistScreenState.Error(e)) }
            }
        }
    }

    fun toggle(playlistId: String) {
        if (_uiState.value.submitting) {
            return
        }
        _uiState.update { currentState ->
            currentState.copy(
                pendingToggles = if (playlistId in currentState.pendingToggles) {
                    currentState.pendingToggles - playlistId
                } else {
                    currentState.pendingToggles + playlistId
                }
            )
        }
    }

    fun createPlaylist(
        title: String,
        description: String,
        privacy: Privacy,
        onStateChanged: () -> Unit = {},
    ) {
        viewModelScope.launch {
            if (_uiState.value.submitting) {
                return@launch
            }
            try {
                val settings = datastoreRepository.getSettings()
                if (settings.cookies.isEmpty()) {
                    throw Exception(application.getString(R.string.failed_get_to_login_cookies))
                }

                _uiState.update { it.copy(submitting = true) }
                var createdPlaylist: PlaylistInfo? = null
                playlistRepository.create(title, description, privacy, settings)
                    .collect { apiResult ->
                        when (apiResult) {
                            is ApiResult.Error -> throw apiResult.exception
                            ApiResult.Loading -> Unit
                            is ApiResult.Success -> createdPlaylist = apiResult.data
                        }
                    }
                onStateChanged()

                val videoId = currentVideoId
                if (videoId == null) {
                    _uiState.update { it.copy(submitting = false) }
                    return@launch
                }
                playlistRepository.retrieveAddToPlaylistOptions(videoId, settings)
                    .collect { apiResult ->
                        _uiState.update { currentState ->
                            currentState.copy(
                                submitting = false,
                                screenState = when (apiResult) {
                                    is ApiResult.Error -> AddToPlaylistScreenState.Error(apiResult.exception)
                                    ApiResult.Loading -> AddToPlaylistScreenState.Loading
                                    is ApiResult.Success -> AddToPlaylistScreenState.Success(
                                        createdPlaylist?.let { info ->
                                            buildList {
                                                if (apiResult.data.none { it.playlistId == info.id }) {
                                                    add(
                                                        AddToPlaylistOption(
                                                            playlistId = info.id,
                                                            title = info.title,
                                                            thumbnailUrl = info.coverHref
                                                                .takeIf { it.isNotBlank() },
                                                        )
                                                    )
                                                }
                                                addAll(apiResult.data)
                                            }
                                        } ?: apiResult.data
                                    )
                                }
                            )
                        }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        screenState = AddToPlaylistScreenState.Error(e),
                        submitting = false,
                    )
                }
            }
        }
    }

    fun confirm(
        song: Song,
        onStateChanged: () -> Unit = {},
        onComplete: () -> Unit = {},
    ) {
        viewModelScope.launch {
            val current = _uiState.value
            if (current.submitting) {
                return@launch
            }
            val success = current.screenState as? AddToPlaylistScreenState.Success ?: return@launch

            if (success.options.isEmpty()) {
                onStateChanged()
                onComplete()
                return@launch
            }

            _uiState.update { it.copy(submitting = true) }
            try {
                val settings = datastoreRepository.getSettings()
                if (settings.cookies.isEmpty()) {
                    throw Exception(application.getString(R.string.failed_get_to_login_cookies))
                }

                current.pendingToggles.forEach { playlistId ->
                    playlistRepository.toggleSongInPlaylist(
                        playlistId = playlistId,
                        song = song,
                        settings = settings,
                        currentlyContains = false,
                    ).collect { apiResult ->
                        if (apiResult is ApiResult.Error) {
                            throw apiResult.exception
                        }
                    }
                }
                _uiState.update {
                    it.copy(
                        pendingToggles = emptySet(),
                        submitting = false,
                    )
                }
                onStateChanged()
                onComplete()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        screenState = AddToPlaylistScreenState.Error(e),
                        submitting = false,
                    )
                }
            }
        }
    }

    fun cancel() {
        if (_uiState.value.submitting) {
            return
        }
        _uiState.update {
            it.copy(
                pendingToggles = emptySet(),
                submitting = false,
            )
        }
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AddToPlaylistViewModel(application)
            }
        }
    }
}