package ca.ilianokokoro.umihi.music.ui.screens.settings

import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.UmihiSettings

data class SettingsState(
    val screenState: ScreenState = ScreenState.Loading,
    val showUpdateChannelDialog: Boolean = false,
    val showDownloadDeleteConfirm: Boolean = false,
    val showCacheSizeInputDialog: Boolean = false,
    val cacheTypeForInput: CacheType = CacheType.AUDIO,
    val showCacheClearConfirm: Boolean = false,
    val showHiddenPlaylistsSheet: Boolean = false,
    val hiddenPlaylists: List<Playlist> = emptyList(),
)

enum class CacheType {
    AUDIO,
    THUMBNAIL
}

sealed class ScreenState {
    data class Success(
        val settings: UmihiSettings,
    ) : ScreenState()

    data object Loading : ScreenState()
    data class Error(val exception: Exception) : ScreenState()
}