package ca.ilianokokoro.umihi.music.models

data class Playlist(
    val id: String,
    val title: String,
    val coverHref: String,
    val songs: List<Song> = emptyList()
)