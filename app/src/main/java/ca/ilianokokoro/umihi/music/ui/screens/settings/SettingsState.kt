package ca.ilianokokoro.umihi.music.ui.screens.settings

data class SettingsState(
    val screenState: ScreenState = ScreenState.Loading
)

sealed class ScreenState {
    data class Success(
        val isLoggedIn: Boolean = false,
    ) : ScreenState()

    data object Loading : ScreenState()
    data class Error(val exception: Exception) : ScreenState()
}