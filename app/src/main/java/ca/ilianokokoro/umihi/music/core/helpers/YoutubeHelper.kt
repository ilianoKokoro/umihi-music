package ca.ilianokokoro.umihi.music.core.helpers

import android.content.Context
import android.util.Log
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.models.Cookies
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import org.schabi.newpipe.extractor.ServiceList

object YoutubeHelper {
    private val client = OkHttpClient()


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

    fun extractPlaylists(jsonString: String): List<PlaylistInfo> {
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

        val playlistInfos = mutableListOf<PlaylistInfo>()

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

                playlistInfos.add(
                    PlaylistInfo(
                        id = browseId,
                        title = playlistTitle,
                        coverHref = thumbnailUrl,
                    ),


                    )
            }
        }

        return playlistInfos
    }

    fun extractHighQualityThumbnail(jsonString: String): String {
        val json = Json.parseToJsonElement(jsonString).jsonObject
        val url = json["videoDetails"]
            ?.jsonObject?.get("thumbnail")
            ?.jsonObject?.get("thumbnails")
            ?.jsonArray?.last()
            ?.jsonObject?.get("url")
            ?.jsonPrimitive?.contentOrNull

        return url ?: ""
    }

    fun extractSongList(jsonString: String, cookies: Cookies): List<Song> {
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
        return parseSongsFromContents(contents, cookies)
    }

    fun extractContinuationSongs(jsonString: String, cookies: Cookies): List<Song> {
        val json = Json.parseToJsonElement(jsonString).jsonObject

        val contents = json["onResponseReceivedActions"]
            ?.jsonArray?.getOrNull(0)
            ?.jsonObject?.get("appendContinuationItemsAction")
            ?.jsonObject?.get("continuationItems")
            ?.jsonArray

        return parseSongsFromContents(contents, cookies)
    }

    private fun parseSongsFromContents(contents: JsonArray?, cookies: Cookies): List<Song> {
        val songs = mutableListOf<Song>()
        if (contents == null) return songs

        for (shelf in contents) {
            val continuationContent = shelf.jsonObject["continuationItemRenderer"]

            if (continuationContent != null) {
                val token = continuationContent.jsonObject["continuationEndpoint"]
                    ?.jsonObject?.get("continuationCommand")
                    ?.jsonObject?.get("token")
                    ?.jsonPrimitive?.contentOrNull ?: ""
                val otherSongs = extractContinuationSongs(
                    YoutubeRequestHelper.requestContinuation(
                        continuationToken = token,
                        cookies = cookies
                    ), cookies
                )
                songs.addAll(otherSongs)
                continue
            }

            val songContent =
                shelf.jsonObject["musicResponsiveListItemRenderer"]?.jsonObject ?: continue
            val thumbnailUrl = getBestThumbnailUrl(songContent["thumbnail"] ?: continue)

            val title = getSongInfo(songContent, SongInfoType.TITLE)
            val artist = getSongInfo(songContent, SongInfoType.ARTIST)
            val duration =
                songContent["fixedColumns"]
                    ?.jsonArray[0]
                    ?.jsonObject["musicResponsiveListItemFixedColumnRenderer"]
                    ?.jsonObject["text"]
                    ?.jsonObject["runs"]
                    ?.jsonArray[0]
                    ?.jsonObject["text"]
                    ?.jsonPrimitive
                    ?.contentOrNull.toString()


            val videoId = songContent["playlistItemData"]
                ?.jsonObject?.get("videoId")
                ?.jsonPrimitive?.contentOrNull ?: continue

            songs.add(
                Song(
                    youtubeId = videoId,
                    title = title,
                    artist = artist,
                    duration = duration,
                    thumbnailHref = thumbnailUrl
                )
            )
        }

        return songs
    }


    suspend fun getSongPlayerUrl(context: Context, songId: String): String {
        val localSongRepository = AppDatabase.getInstance(context).songRepository()

        val savedSong = localSongRepository.getSongById(songId)
        if (savedSong != null && savedSong.streamUrl != null) {
            if (isYoutubeUrlValid(savedSong.streamUrl)) {
                Log.d("CustomLog", "$songId : Got url from saved")
                return savedSong.streamUrl
            }
            Log.d("CustomLog", "$songId : Saved url was invalid")
            localSongRepository.delete(savedSong)
        }

        val newUri = getSongUrlFromYoutube(songId)
        val newSong =
            Song(youtubeId = songId, streamUrl = newUri)
        localSongRepository.create(newSong)
        Log.d("CustomLog", "$songId : Got url from YouTube and saved song")
        return newUri
    }

    private suspend fun getSongUrlFromYoutube(songId: String): String {
        val service = ServiceList.YouTube
        val extractor = withContext(Dispatchers.IO) {
            val extractor =
                service.getStreamExtractor(Song(youtubeId = songId).youtubeUrl)
            extractor.fetchPage()
            return@withContext extractor
        }
        return extractor.audioStreams.maxBy { it.averageBitrate }.content
    }


    private suspend fun isYoutubeUrlValid(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .head()
                .build()

            client.newCall(request).execute().use { response ->
                return@withContext response.isSuccessful
            }
        } catch (e: Exception) {
            return@withContext false
        }
    }

}


enum class SongInfoType(val index: Int) {
    TITLE(0),
    ARTIST(1),
}