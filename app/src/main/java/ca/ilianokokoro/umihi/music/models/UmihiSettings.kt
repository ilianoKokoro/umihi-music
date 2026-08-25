package ca.ilianokokoro.umihi.music.models

import androidx.compose.runtime.Immutable
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository.UpdateChannel

@Immutable
data class UmihiSettings(
    val updateChannel: UpdateChannel = UpdateChannel.Stable,
    val updateChecking: Boolean = true,
    val cookies: Cookies,
    val dataSyncId: String?,
    val showPodcastPlaylist: Boolean = true,
    val useSpecialLanguage: Boolean = false,
    val useAudioOffload: Boolean = false,
    val keepScreenOn: Boolean = false,
    val sendPlaybackData: Boolean = false,
    val downloadOnMetered: Boolean = false,
    val exoPlayerCacheSizeMB: Int = Constants.ExoPlayer.Cache.DEFAULT_SIZE_MB.toInt(),
    val thumbnailCacheSizeMB: Int = Constants.ExoPlayer.ThumbnailCache.DEFAULT_SIZE_MB.toInt()
) {
    val canTrack: Boolean get() = sendPlaybackData && !cookies.isEmpty()
}