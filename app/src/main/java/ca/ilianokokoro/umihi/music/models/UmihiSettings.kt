package ca.ilianokokoro.umihi.music.models

import androidx.media3.common.util.UnstableApi
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository.UpdateChannel

@UnstableApi
data class UmihiSettings(
    val updateChannel: UpdateChannel = UpdateChannel.Stable,
    val cookies: Cookies,
    val dataSyncId: String,
    val showPodcastPlaylist: Boolean = true,
    val useSpecialLanguage: Boolean = false,
    val useAudioOffload: Boolean = false
)