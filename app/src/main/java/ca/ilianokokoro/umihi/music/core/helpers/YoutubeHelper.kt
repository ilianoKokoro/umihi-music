package ca.ilianokokoro.umihi.music.core.helpers

import android.util.Log
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.Song
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object YoutubeHelper {
    fun getBestThumbnailUrl(thumbnailElement: JsonElement): String {
        val url =
            thumbnailElement.jsonObject["musicThumbnailRenderer"]?.jsonObject?.get("thumbnail")?.jsonObject?.get(
                "thumbnails"
            )?.jsonArray?.last()?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull ?: ""
        return url
    }

    fun getSongInfo(songMap: JsonElement, songInfoIndex: SongInfoType): String {
        return songMap.jsonObject["flexColumns"]
            ?.jsonArray?.getOrNull(songInfoIndex.index)
            ?.jsonObject?.get("musicResponsiveListItemFlexColumnRenderer")
            ?.jsonObject?.get("text")
            ?.jsonObject?.get("runs")
            ?.jsonArray?.getOrNull(0)
            ?.jsonObject?.get("text")
            ?.jsonPrimitive?.contentOrNull ?: ""
    }

    fun extractPlaylists(jsonString: String): List<Playlist> {
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

                val thumbnailUrl = getBestThumbnailUrl(
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


    fun extractSongList(jsonString: String): List<Song> {
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

            val thumbnailUrl = getBestThumbnailUrl(
                songContent["thumbnail"] ?: continue
            )

            val title = getSongInfo(songContent, SongInfoType.TITLE)
            val artist = getSongInfo(songContent, SongInfoType.ARTIST)

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


    fun extractHighQualityThumbnail(jsonString: String): String {
        Log.d("CustomLog", jsonString)
        return ""
    }


}


enum class SongInfoType(val index: Int) {
    TITLE(0),
    ARTIST(1),
}