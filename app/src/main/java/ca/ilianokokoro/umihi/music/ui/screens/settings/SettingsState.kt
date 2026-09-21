package ca.ilianokokoro.umihi.music.ui.screens.settings

import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.UmihiSettings

data class SettingsState(
    val screenState: ScreenState = ScreenState.Loading,
    val showUpdateChannelSheet: Boolean = false,
    val showDownloadDeleteConfirm: Boolean = false,
    val showCacheSizeInputSheet: Boolean = false,
    val showLoginClearConfirm: Boolean = false,
    val cacheTypeForInput: CacheType = CacheType.AUDIO,
    val showCacheClearConfirm: Boolean = false,
    val showHiddenPlaylistsSheet: Boolean = false,
    val hiddenPlaylists: List<Playlist> = emptyList(),
    val showDiagnosticsLogsSheet: Boolean = false,
    val showThemeSelectorSheet: Boolean = false,
    val showDownloadLocationDialog: Boolean = false,
    val audioCacheUsed: Long = 0L,
    val thumbnailCacheUsed: Long = 0L,
    val downloadsUsage: DownloadsUsage = DownloadsUsage(),
)

data class DownloadsUsage(
    val audioBytes: Long = 0L,
    val imageBytes: Long = 0L,
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