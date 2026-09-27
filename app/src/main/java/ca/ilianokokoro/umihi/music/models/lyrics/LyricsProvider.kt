package ca.ilianokokoro.umihi.music.models.lyrics


interface LyricsProvider {
    val name: String
    suspend fun getLyrics(query: LyricsQuery): Lyrics?
}