package ca.ilianokokoro.umihi.music.core.helpers

import android.content.Context
import android.widget.Toast
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printd
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printe
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.models.UmihiSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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


    fun extractSearchResults(jsonString: String): List<Song> {
        val json = Json.parseToJsonElement(jsonString).jsonObject

        val tabs = json["contents"]
            ?.jsonObject?.get("tabbedSearchResultsRenderer")
            ?.jsonObject?.get("tabs")
            ?.jsonArray ?: return emptyList()


        val selectedTab = tabs.firstOrNull {
            it.jsonObject["tabRenderer"]
                ?.jsonObject?.get("selected")
                ?.jsonPrimitive?.booleanOrNull == true
        }?.jsonObject?.get("tabRenderer")?.jsonObject ?: return emptyList()

        val contents = selectedTab["content"]
            ?.jsonObject?.get("sectionListRenderer")
            ?.jsonObject?.get("contents")
            ?.jsonArray ?: return emptyList()

        val songRendererList =
            contents.jsonArray[0]
                .jsonObject["musicShelfRenderer"]
                ?.jsonObject["contents"]
                ?.jsonArray
                ?: return emptyList()

        return songRendererList.mapNotNull { extractSong(it) }
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

    fun extractSongList(jsonString: String, settings: UmihiSettings): List<Song> {
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
        return parseSongsFromContents(contents, settings)
    }

    fun extractContinuationSongs(jsonString: String, settings: UmihiSettings): List<Song> {
        val json = Json.parseToJsonElement(jsonString).jsonObject

        val contents = json["onResponseReceivedActions"]
            ?.jsonArray?.getOrNull(0)
            ?.jsonObject?.get("appendContinuationItemsAction")
            ?.jsonObject?.get("continuationItems")
            ?.jsonArray

        return parseSongsFromContents(contents, settings)
    }

    private fun parseSongsFromContents(
        contents: JsonArray?,
        settings: UmihiSettings
    ): List<Song> {
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
                        settings = settings
                    ), settings
                )
                songs.addAll(otherSongs)

                continue
            }


            val song = extractSong(shelf) ?: continue
            songs.add(
                song
            )
        }

        return songs
    }

    fun extractSong(
        json: JsonElement,
    ): Song? {
        val songContent =
            json.jsonObject["musicResponsiveListItemRenderer"]?.jsonObject ?: return null
        val thumbnailUrl = getBestThumbnailUrl(songContent["thumbnail"] ?: return null)

        val title = getSongInfo(songContent, SongInfoType.TITLE)
        val artist = getSongInfo(songContent, SongInfoType.ARTIST)
        val videoId = songContent["playlistItemData"]
            ?.jsonObject?.get("videoId")
            ?.jsonPrimitive?.contentOrNull ?: return null


        var duration = ""
        val fixedColumn =
            songContent["fixedColumns"]?.jsonArray[0]?.jsonObject["musicResponsiveListItemFixedColumnRenderer"]
        val flexColumn =
            songContent["flexColumns"]?.jsonArray[1]?.jsonObject["musicResponsiveListItemFlexColumnRenderer"]

        if (fixedColumn != null) {
            duration = fixedColumn.jsonObject["text"]
                ?.jsonObject["runs"]
                ?.jsonArray[0]
                ?.jsonObject["text"]
                ?.jsonPrimitive
                ?.contentOrNull.toString()
        } else if (flexColumn != null) {
            duration = flexColumn.jsonObject["text"]
                ?.jsonObject["runs"]
                ?.jsonArray[4]
                ?.jsonObject["text"]
                ?.jsonPrimitive
                ?.contentOrNull.toString()
        }

        
        return Song(
            youtubeId = videoId,
            title = title,
            artist = artist,
            duration = duration,
            thumbnailHref = thumbnailUrl
        )

    }


    suspend fun getSongPlayerUrl(
        context: Context,
        songId: String,
        allowLocal: Boolean = false
    ): String {
        val localSongRepository = AppDatabase.getInstance(context).songRepository()
        var savedSong: Song? = null
        try {
            savedSong = localSongRepository.getSong(songId)
        } catch (ex: Exception) {
            Toast.makeText(context, "Failed to get song from local repository", Toast.LENGTH_LONG)
                .show()
            printe(ex.toString())
        }

        if (savedSong != null) {
            if (allowLocal && savedSong.audioFilePath != null) {
                printd("$songId : Was downloaded")
                return savedSong.audioFilePath
            }

            if (savedSong.streamUrl != null) {
                if (isYoutubeUrlValid(savedSong.streamUrl)) {
                    printd("$songId : Got url from saved")
                    return savedSong.streamUrl
                }
                printd("$songId : Saved url was invalid")
            }
        }

        val newUri = getSongUrlFromYoutube(songId)
        localSongRepository.setStreamUrl(songId = songId, streamUrl = newUri)
        printd("$songId : Got url from YouTube and saved song")
        return newUri
    }

    private suspend fun getSongUrlFromYoutube(
        songId: String,
        retries: Int = Constants.YoutubeApi.RETRY_COUNT
    ): String {
        val service = ServiceList.YouTube

        var attempts = 0

        repeat(retries) { attempt ->
            try {
                attempts++
                val streamUrl = withContext(Dispatchers.IO) {
                    val extractor = service.getStreamExtractor(Song(youtubeId = songId).youtubeUrl)
                    extractor.fetchPage()
                    extractor.audioStreams.maxBy { it.averageBitrate }.content
                }

                return streamUrl
            } catch (e: Exception) {
                printe(
                    "Failed to get song $songId from Youtube : Attempt -> $attempts/$retries : ${e.message}"
                )
                delay(Constants.YoutubeApi.RETRY_DELAY * (attempt + 1))
            }
        }

        throw Exception("Fatal fail for song $songId. Could not get it after $attempts attempts")
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