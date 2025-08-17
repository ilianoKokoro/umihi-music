package ca.ilianokokoro.umihi.music.data.datasources

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.YoutubeHelper
import ca.ilianokokoro.umihi.music.core.helpers.YoutubeRequestHelper
import ca.ilianokokoro.umihi.music.models.Cookies
import ca.ilianokokoro.umihi.music.models.Playlist

class PlaylistDataSource {
    fun retrieveAll(cookies: Cookies): List<Playlist> {
        return YoutubeHelper.extractPlaylists(
            YoutubeRequestHelper.browse(
                Constants.YoutubeApi.Browse.PLAYLIST_BROWSE_ID,
                cookies
            )
        )
    }

    fun retrieveOne(playlist: Playlist, cookies: Cookies): Playlist {
        return playlist.copy(
            songs = YoutubeHelper.extractSongList(
                YoutubeRequestHelper.browse(
                    playlist.id,
                    cookies
                )
            )
        )
    }


}
