package ca.ilianokokoro.umihi.music.data.datasources

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.YoutubeHelper
import ca.ilianokokoro.umihi.music.core.helpers.YoutubeRequestHelper
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.UmihiSettings

class PlaylistDataSource {
    fun retrieveAll(settings: UmihiSettings): List<PlaylistInfo> {
        return YoutubeHelper.extractPlaylists(
            YoutubeRequestHelper.browse(
                Constants.YoutubeApi.Browse.PLAYLIST_BROWSE_ID,
                settings
            )
        )
    }

    fun retrieveOne(playlist: Playlist, settings: UmihiSettings): Playlist {
        return playlist.copy(
            songs = YoutubeHelper.extractSongList(
                YoutubeRequestHelper.browse(
                    playlist.info.id,
                    settings
                ), settings
            )
        )
    }


}
