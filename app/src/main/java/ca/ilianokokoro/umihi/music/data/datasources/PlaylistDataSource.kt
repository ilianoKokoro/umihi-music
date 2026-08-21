package ca.ilianokokoro.umihi.music.data.datasources

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.youtube.YoutubeApiClient
import ca.ilianokokoro.umihi.music.core.youtube.YoutubeDataExtractor
import ca.ilianokokoro.umihi.music.models.HomeSection
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.Privacy
import ca.ilianokokoro.umihi.music.models.UmihiSettings

class PlaylistDataSource {
    suspend fun retrieveHomeSections(settings: UmihiSettings): List<HomeSection> {
        return YoutubeDataExtractor.extractHomeSections(
            YoutubeApiClient.browseHome(settings),
            settings
        )
    }

    suspend fun retrieveChartsSections(settings: UmihiSettings): List<HomeSection> {
        return try {
            val result = YoutubeDataExtractor.extractHomeSections(
                YoutubeApiClient.browse(Constants.YoutubeApi.Browse.CHARTS_BROWSE_ID, settings),
                settings
            )
            if (result.isNotEmpty()) result else retrieveHomeSections(settings)
        } catch (_: Exception) {
            retrieveHomeSections(settings)
        }
    }

    suspend fun retrieveMoodSections(query: String, title: String, settings: UmihiSettings): List<HomeSection> {
        return try {
            val songs = YoutubeDataExtractor.extractSearchResults(
                YoutubeApiClient.search(
                    query = query,
                    filterParams = Constants.YoutubeApi.Search.FILTER_SONGS,
                    settings = settings
                )
            )
            if (songs.isNotEmpty()) {
                listOf(
                    HomeSection(
                        id = query,
                        title = title,
                        subtitle = null,
                        items = songs.map { ca.ilianokokoro.umihi.music.models.HomeSectionItem.SongItem(it) }
                    )
                )
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

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
