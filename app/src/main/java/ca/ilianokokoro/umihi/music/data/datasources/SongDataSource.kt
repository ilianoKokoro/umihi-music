package ca.ilianokokoro.umihi.music.data.datasources

import ca.ilianokokoro.umihi.music.core.helpers.YoutubeHelper
import ca.ilianokokoro.umihi.music.core.helpers.YoutubeRequestHelper

class SongDataSource {
    fun getSongThumbnail(songId: String): String {
        return YoutubeHelper.extractHighQualityThumbnail(
            YoutubeRequestHelper.getPlayerInfo(songId)
        )
    }
}