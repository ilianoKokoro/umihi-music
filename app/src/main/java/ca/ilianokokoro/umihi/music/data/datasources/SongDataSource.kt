package ca.ilianokokoro.umihi.music.data.datasources

import ca.ilianokokoro.umihi.music.core.youtube.YoutubeApiClient
import ca.ilianokokoro.umihi.music.core.youtube.YoutubeDataExtractor
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.models.UmihiSettings
import ca.ilianokokoro.umihi.music.models.youtube.MixPage

class SongDataSource {
    suspend fun getSongInfo(songId: String): Song {
        return YoutubeDataExtractor.extractSongInfo(
            YoutubeApiClient.getPlayerInfo(
                songId,
                //  fields = Constants.YoutubeApi.PlayerInfo.Fields.SONG_INFO
            )
        )
    }

    suspend fun getMix(
        settings: UmihiSettings,
        videoId: String? = null,
        playlistId: String? = null,
    ): MixPage {
        return YoutubeDataExtractor.extractMixPage(
            YoutubeApiClient.getMix(
                settings = settings,
                videoId = videoId,
                playlistId = playlistId
            )
        )
    }

    suspend fun getMixContinuation(
        continuation: String,
        settings: UmihiSettings
    ): MixPage {
        return YoutubeDataExtractor.extractMixPage(
            YoutubeApiClient.getMixContinuation(continuation, settings)
        )
    }

    suspend fun searchAutoComplete(query: String): List<String> {
        return YoutubeDataExtractor.extractSearchAutocompleteResults(
            YoutubeApiClient.searchAutoComplete(
                query
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
