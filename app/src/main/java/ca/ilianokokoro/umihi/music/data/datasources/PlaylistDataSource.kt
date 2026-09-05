package ca.ilianokokoro.umihi.music.data.datasources

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.youtube.YoutubeApiClient
import ca.ilianokokoro.umihi.music.core.youtube.YoutubeDataExtractor
import ca.ilianokokoro.umihi.music.models.AddToPlaylistOption
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.Privacy
import ca.ilianokokoro.umihi.music.models.UmihiSettings

class PlaylistDataSource {
    suspend fun retrieveAll(settings: UmihiSettings): List<PlaylistInfo> {
        return YoutubeDataExtractor.extractPlaylists(
            YoutubeApiClient.browse(
                Constants.YoutubeApi.Browse.PLAYLIST_BROWSE_ID,
                settings,
                //  fields = Constants.YoutubeApi.Browse.Fields.PLAYLISTS,
            ), settings
        )
    }

    suspend fun retrieveOne(playlist: Playlist, settings: UmihiSettings): Playlist {
        return playlist.copy(
            songs = YoutubeDataExtractor.extractSongList(
                YoutubeApiClient.browse(
                    playlist.info.id,
                    settings,
                    //   fields = Constants.YoutubeApi.Browse.Fields.SONGS,
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

    suspend fun removeFromLibrary(
        playlist: PlaylistInfo,
        settings: UmihiSettings
    ) {
        YoutubeApiClient.removePlaylistFromLibrary(
            playlist,
            settings = settings
        )
    }

    suspend fun retrieveAddToPlaylistOptions(
        videoId: String,
        settings: UmihiSettings
    ): List<AddToPlaylistOption> {
        return YoutubeDataExtractor.extractAddToPlaylistOptions(
            YoutubeApiClient.getAddToPlaylists(
                videoId = videoId,
                settings = settings
            )
        )
    }

    suspend fun findSetVideoId(
        playlistId: String,
        videoId: String,
        settings: UmihiSettings
    ): String {
        val browseId = "VL${playlistId.removePrefix("VL")}"
        val playlist = retrieveOne(Playlist(PlaylistInfo(id = browseId)), settings)
        return playlist.songs
            .firstOrNull { it.youtubeId == videoId }
            ?.setVideoId
            ?: throw IllegalStateException("Track not found in playlist $browseId")
    }

    suspend fun edit(
        playlistId: String,
        settings: UmihiSettings,
        title: String? = null,
        description: String? = null,
        privacy: Privacy? = null,
        videoIdsToAdd: List<String>? = null,
        setVideoIdsToRemove: List<String>? = null,
    ) {
        YoutubeApiClient.editPlaylist(
            playlistId = playlistId,
            settings = settings,
            title = title,
            description = description,
            privacy = privacy,
            videoIdsToAdd = videoIdsToAdd,
            setVideoIdsToRemove = setVideoIdsToRemove,
        )
    }
}
