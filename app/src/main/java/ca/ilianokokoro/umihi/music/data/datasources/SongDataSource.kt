package ca.ilianokokoro.umihi.music.data.datasources

import ca.ilianokokoro.umihi.music.models.Song

class SongDataSource {
    fun getStreamUrlFromId(song: Song): String {
        return song.id // TODO
    }

}