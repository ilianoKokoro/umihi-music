package ca.ilianokokoro.umihi.music.data.datasources

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.SongInfoType
import ca.ilianokokoro.umihi.music.core.helpers.YoutubeHelper
import ca.ilianokokoro.umihi.music.core.helpers.YoutubeRequestHelper
import ca.ilianokokoro.umihi.music.models.Cookies
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.Song
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class PlaylistDataSource {
    fun retrieveAll(cookies: Cookies): List<Playlist> {
        return extractPlaylistsFromJson(
            YoutubeRequestHelper.browse(
                Constants.YoutubeApi.Browse.PLAYLIST_BROWSE_ID,
                cookies
            )
        )
    }

    fun retrieveOne(playlist: Playlist, cookies: Cookies): Playlist {
        return playlist.copy(
            songs = extractSongListFromJson(
                YoutubeRequestHelper.browse(
                    playlist.id,
                    cookies
                )
            )
        )
    }

    private fun extractPlaylistsFromJson(jsonString: String): List<Playlist> {
        val json = Json.parseToJsonElement(jsonString).jsonObject

        val tabs = json["contents"]
            ?.jsonObject?.get("singleColumnBrowseResultsRenderer")
            ?.jsonObject?.get("tabs")
            ?.jsonArray ?: return emptyList()

        val selectedTab = tabs.firstOrNull {
            it.jsonObject["tabRenderer"]
                ?.jsonObject?.get("selected")
                ?.jsonPrimitive?.booleanOrNull == true
        }?.jsonObject?.get("tabRenderer")?.jsonObject ?: return emptyList()

        val sectionList = selectedTab["content"]
            ?.jsonObject?.get("sectionListRenderer")
            ?.jsonObject?.get("contents")
            ?.jsonArray ?: return emptyList()

        val playlists = mutableListOf<Playlist>()

        for (section in sectionList) {
            val gridItems = section.jsonObject["gridRenderer"]
                ?.jsonObject?.get("items")
                ?.jsonArray ?: continue

            for (item in gridItems) {
                val playlistShelf =
                    item.jsonObject["musicTwoRowItemRenderer"]?.jsonObject ?: continue

                val playlistTitle = playlistShelf["title"]
                    ?.jsonObject?.get("runs")
                    ?.jsonArray?.getOrNull(0)
                    ?.jsonObject?.get("text")
                    ?.jsonPrimitive?.contentOrNull ?: continue

                val browseId = playlistShelf["navigationEndpoint"]
                    ?.jsonObject?.get("browseEndpoint")
                    ?.jsonObject?.get("browseId")
                    ?.jsonPrimitive?.contentOrNull ?: continue

                val thumbnailUrl = YoutubeHelper.getBestThumbnailUrl(
                    playlistShelf["thumbnailRenderer"] ?: continue
                )

                playlists.add(
                    Playlist(
                        id = browseId,
                        title = playlistTitle,
                        coverHref = thumbnailUrl,
                        songs = emptyList()
                    )
                )
            }
        }

        return playlists
    }


    private fun extractSongListFromJson(jsonString: String): List<Song> {
        val json = Json.parseToJsonElement(jsonString).jsonObject

        val contents = json["contents"]
            ?.jsonObject?.get("twoColumnBrowseResultsRenderer")
            ?.jsonObject?.get("secondaryContents")
            ?.jsonObject?.get("sectionListRenderer")
            ?.jsonObject?.get("contents")
            ?.jsonArray?.getOrNull(0)
            ?.jsonObject?.get("musicPlaylistShelfRenderer")
            ?.jsonObject?.get("contents")
            ?.jsonArray

        val songs = mutableListOf<Song>()

        if (contents == null) {
            return listOf()
        }

        for (shelf in contents) {
            val songContent =
                shelf.jsonObject["musicResponsiveListItemRenderer"]?.jsonObject ?: continue

            val thumbnailUrl = YoutubeHelper.getBestThumbnailUrl(
                songContent["thumbnail"] ?: continue
            )

            val title = YoutubeHelper.getSongInfo(songContent, SongInfoType.TITLE)
            val artist = YoutubeHelper.getSongInfo(songContent, SongInfoType.ARTIST)

            val videoId = songContent["playlistItemData"]
                ?.jsonObject?.get("videoId")
                ?.jsonPrimitive?.contentOrNull ?: continue

            songs.add(
                Song(
                    id = videoId,
                    title = title,
                    artist = artist,
                    lowQualityCoverHref = thumbnailUrl
                )
            )
        }

        return songs
    }
}
