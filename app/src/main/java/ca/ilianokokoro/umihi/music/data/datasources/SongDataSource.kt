package ca.ilianokokoro.umihi.music.data.datasources

import ca.ilianokokoro.umihi.music.core.youtube.YoutubeApiClient
import ca.ilianokokoro.umihi.music.core.youtube.YoutubeDataExtractor
import ca.ilianokokoro.umihi.music.models.Song

import ca.ilianokokoro.umihi.music.models.UmihiSettings

class SongDataSource {
    suspend fun getSongInfo(songId: String): Song {
        return YoutubeDataExtractor.extractSongInfo(
            YoutubeApiClient.getPlayerInfo(songId)
        )
    }

    suspend fun search(
        query: String,
        filterParams: String? = null,
        settings: UmihiSettings? = null
    ): List<Song> {
        return YoutubeDataExtractor.extractSearchResults(
            YoutubeApiClient.search(
                query = query,
                filterParams = filterParams,
                settings = settings
            )
        )
    }

    suspend fun getRelatedSongs(
        videoId: String,
        settings: UmihiSettings? = null
    ): List<Song> {
        return YoutubeDataExtractor.extractRelatedSongs(
            YoutubeApiClient.getNext(
                videoId = videoId,
                settings = settings
            )
        )
    }
}