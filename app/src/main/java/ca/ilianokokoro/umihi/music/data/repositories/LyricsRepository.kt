package ca.ilianokokoro.umihi.music.data.repositories

import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.models.lyrics.Lyrics
import ca.ilianokokoro.umihi.music.models.lyrics.LyricsQuery
import ca.ilianokokoro.umihi.music.models.lyrics.providers.BetterLyricsProvider
import ca.ilianokokoro.umihi.music.models.lyrics.providers.LrcLibProvider

class LyricsRepository {
    suspend fun getLyrics(song: Song): Lyrics? {
        val query = LyricsQuery.fromSong(song)
        for (provider in ORDER) {
            val lyrics = provider.getLyrics(query)

            if (lyrics != null) {
                return lyrics
            }
        }
        return null
    }

    companion object {
        private val ORDER = listOf(
            LrcLibProvider(),
            BetterLyricsProvider(),
        )
    }
}
