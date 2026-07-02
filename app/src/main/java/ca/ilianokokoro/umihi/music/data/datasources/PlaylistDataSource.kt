package ca.ilianokokoro.umihi.music.data.datasources

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.youtube.YoutubeApiClient
import ca.ilianokokoro.umihi.music.core.youtube.YoutubeDataExtractor
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.Privacy
import ca.ilianokokoro.umihi.music.models.UmihiSettings

class PlaylistDataSource {
    suspend fun retrieveAll(settings: UmihiSettings): List<PlaylistInfo> {
        return YoutubeDataExtractor.extractPlaylists(
            YoutubeApiClient.browse(
                Constants.YoutubeApi.Browse.PLAYLIST_BROWSE_ID,
                settings
            ), settings
        )
    }

    suspend fun retrieveOne(playlist: Playlist, settings: UmihiSettings): Playlist {
        return playlist.copy(
            songs = YoutubeDataExtractor.extractSongList(
                YoutubeApiClient.browse(
                    playlist.info.id,
                    settings
                ), settings
            )
        )
    }

    suspend fun create(
        title: String,
        description: String,
        privacy: Privacy,
        settings: UmihiSettings
    ): PlaylistInfo? {

        return YoutubeDataExtractor.extractCreatedPlaylist(
            YoutubeApiClient.createPlaylist(
                title,
                description,
                privacy,
                settings = settings
            )
        )
    }

    suspend fun delete(
        playlist: PlaylistInfo,
        settings: UmihiSettings
    ) {
        YoutubeApiClient.deletePlaylist(
            playlist,
            settings = settings
        )
    }
}
