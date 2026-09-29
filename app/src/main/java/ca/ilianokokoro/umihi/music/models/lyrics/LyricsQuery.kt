package ca.ilianokokoro.umihi.music.models.lyrics

import ca.ilianokokoro.umihi.music.models.Song

data class LyricsQuery(
    val title: String,
    val artist: String,
    val durationMs: Long,
    val album: String = "",
) {
    companion object {
        fun fromSong(song: Song): LyricsQuery {
            val durationSeconds = song.durationSeconds ?: 0
            return LyricsQuery(
                title = song.title,
                artist = song.artist,
                durationMs = durationSeconds * 1_000L
            )
        }
    }
}