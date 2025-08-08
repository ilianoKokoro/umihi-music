package ca.ilianokokoro.umihi.music.models

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class Playlist(
    val id: String,
    val title: String,
    val coverHref: String,
    val songs: List<Song> = emptyList()
)