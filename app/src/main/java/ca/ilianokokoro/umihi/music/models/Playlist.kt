package ca.ilianokokoro.umihi.music.models

import kotlinx.serialization.Serializable

@Serializable
data class Playlist(
    val id: String,
    val title: String,
    val coverHref: String,
    val songs: List<Song> = emptyList()
)