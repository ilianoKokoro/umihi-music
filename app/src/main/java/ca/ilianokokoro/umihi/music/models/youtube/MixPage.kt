package ca.ilianokokoro.umihi.music.models.youtube

import ca.ilianokokoro.umihi.music.models.Song

data class MixPage(
    val songs: List<Song> = emptyList(),
    val continuation: String? = null,
)
