package ca.ilianokokoro.umihi.music.data.datasources

import ca.ilianokokoro.umihi.music.core.helpers.YoutubeHelper
import ca.ilianokokoro.umihi.music.core.helpers.YoutubeRequestHelper
import ca.ilianokokoro.umihi.music.models.Song

class SongDataSource {
    fun getSongThumbnail(songId: String): String {
        return YoutubeHelper.extractHighQualityThumbnail(
            YoutubeRequestHelper.getPlayerInfo(songId)
        )
    }

    fun search(query: String): List<Song> {
        return YoutubeHelper.extractSearchResults(
            YoutubeRequestHelper.search(
                query
            )
        )
    }
}