package ca.ilianokokoro.umihi.music.models

import androidx.compose.runtime.Immutable
import ca.ilianokokoro.umihi.music.core.Constants
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnail: String,
) {
    val uri: String
        get() = "${Constants.YoutubeApi.YOUTUBE_URL_PREFIX}${id}"
}