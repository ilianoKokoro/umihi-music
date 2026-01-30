package ca.ilianokokoro.umihi.music.ui.screens.search

import ca.ilianokokoro.umihi.music.models.Song


data class SearchState(
    val screenState: ScreenState = ScreenState.Success(),
)


sealed class ScreenState {
    data class Success(
        val results: List<Song> = listOf()
    ) : ScreenState()
    
    data object Loading : ScreenState()
    data class Error(val exception: Exception) : ScreenState()
}