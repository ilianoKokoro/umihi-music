package ca.ilianokokoro.umihi.music.data.datasources

import android.util.Log
import ca.ilianokokoro.umihi.music.core.helpers.YoutubeRequestHelper
import ca.ilianokokoro.umihi.music.models.Song

class SongDataSource {
    fun getStreamUrlFromId(song: Song): String {

        val result = YoutubeRequestHelper.getPlayer(song.id)
        Log.d("CustomLog", result)

        return "" // TODO
    }

}