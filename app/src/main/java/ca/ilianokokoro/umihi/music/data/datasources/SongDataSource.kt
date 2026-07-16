package ca.ilianokokoro.umihi.music.data.datasources

import ca.ilianokokoro.umihi.music.core.youtube.YoutubeApiClient
import ca.ilianokokoro.umihi.music.core.youtube.YoutubeDataExtractor
import ca.ilianokokoro.umihi.music.models.Song

class SongDataSource {
    suspend fun getSongInfo(songId: String): Song {
        return YoutubeDataExtractor.extractSongInfo(
            YoutubeApiClient.getPlayerInfo(
                songId,
                //  fields = Constants.YoutubeApi.PlayerInfo.Fields.SONG_INFO
            )
        )
    }

    suspend fun search(query: String): List<Song> {
        return YoutubeDataExtractor.extractSearchResults(
            YoutubeApiClient.search(
                query
            )
        )
    }
}