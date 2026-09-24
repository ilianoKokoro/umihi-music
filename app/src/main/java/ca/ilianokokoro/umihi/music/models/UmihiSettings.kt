package ca.ilianokokoro.umihi.music.models

import android.net.Uri
import androidx.compose.runtime.Immutable
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository.UpdateChannel

@Immutable
data class UmihiSettings(
    val updateChannel: UpdateChannel,
    val updateChecking: Boolean,
    val cookies: Cookies,
    val dataSyncId: String?,
    val useSpecialLanguage: Boolean,
    val useAudioOffload: Boolean,
    val keepScreenOn: Boolean,
    val sendPlaybackData: Boolean,
    val downloadOnMetered: Boolean,
    val offlineMode: Boolean,
    val exoPlayerCacheSizeMB: Int,
    val thumbnailCacheSizeMB: Int,
    val appVolume: Int,
    val themeMode: ThemeMode,
    val downloadLocation: Uri?
) {
    val canTrack: Boolean get() = sendPlaybackData && !cookies.isEmpty()
}