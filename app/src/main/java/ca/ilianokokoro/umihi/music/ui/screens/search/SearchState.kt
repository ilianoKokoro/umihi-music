package ca.ilianokokoro.umihi.music.ui.screens.search

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.models.Song

enum class SearchFilter(val params: String?) {
    ALL(null),
    SONGS(Constants.YoutubeApi.Search.FILTER_SONGS),
    VIDEOS(Constants.YoutubeApi.Search.FILTER_VIDEOS),
}

data class SearchState(
    val search: String = String(),
    val activeFilter: SearchFilter = SearchFilter.ALL,
    val screenState: ScreenState = ScreenState.Success(),
)

sealed class ScreenState {
    data class Success(
        val results: List<Song> = listOf()
    ) : ScreenState()

    data object Loading : ScreenState()
    data class Error(val exception: Exception) : ScreenState()
}