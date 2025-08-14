package ca.ilianokokoro.umihi.music.data.datasources

import android.util.Log
import ca.ilianokokoro.umihi.music.core.helpers.YoutubeRequestHelper
import ca.ilianokokoro.umihi.music.models.Cookies
import ca.ilianokokoro.umihi.music.models.Song

class SongDataSource {
    fun getStreamUrlFromId(song: Song, cookies: Cookies): String {

        val result = YoutubeRequestHelper.getPlayer(song.id, cookies)
        Log.d("CustomLog", result)

        return "" // TODO
    }

}