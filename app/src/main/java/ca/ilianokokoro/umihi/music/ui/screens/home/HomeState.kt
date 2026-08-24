package ca.ilianokokoro.umihi.music.ui.screens.home

import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.models.HomeSection
import ca.ilianokokoro.umihi.music.models.PlaylistInfo

enum class HomeCategory(val titleRes: Int, val iconEmoji: String) {
    FOR_YOU(R.string.category_for_you, "✨"),
    CHARTS(R.string.category_charts, "🔥"),
    CHILL(R.string.category_chill, "☕"),
    WORKOUT(R.string.category_workout, "⚡"),
    FOCUS(R.string.category_focus, "📚"),
    SLEEP(R.string.category_sleep, "🌙")
}

data class HomeState(
    val screenState: ScreenState = ScreenState.Loading,
    val isRefreshing: Boolean = false,
    val selectedCategory: HomeCategory = HomeCategory.FOR_YOU,
    val timeGreetingRes: Int = R.string.greeting_morning,
    val timeGreetingEmoji: String = "☀️"
)

sealed class ScreenState {
    data class LoggedIn(
        val sections: List<HomeSection> = emptyList(),
        val playlistInfos: List<PlaylistInfo> = emptyList(),
        val isLoggedIn: Boolean = false,
    ) : ScreenState()

    data object Loading : ScreenState()
    data class Error(val exception: Exception) : ScreenState()
}