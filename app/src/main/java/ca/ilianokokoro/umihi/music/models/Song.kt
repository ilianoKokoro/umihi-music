package ca.ilianokokoro.umihi.music.models

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnail: String,
)