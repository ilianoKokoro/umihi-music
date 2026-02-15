package ca.ilianokokoro.umihi.music.core.helpers

import android.content.Context
import android.widget.Toast
import androidx.core.net.toUri
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
import java.util.Locale

object YoutubeHelper {
    private val client = OkHttpClient()


    fun extractYouTubeVideoId(url: String): String? {
        val uri = url.toUri()

        return when {
            uri.host?.contains("youtu.be") == true -> uri.lastPathSegment
            uri.host?.contains("youtube.com") == true || uri.host?.contains("music.youtube.com") == true -> uri.getQueryParameter(
                "v"
            )

            else -> null
        }
    }

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

    fun extractPlaylists(
        jsonString: String,
        settings: UmihiSettings
    ): List<PlaylistInfo> {
        val json = Json.parseToJsonElement(jsonString).jsonObject
        val playlistInfos = mutableListOf<PlaylistInfo>()

        val tabs = json["contents"]
            ?.jsonObject?.get("singleColumnBrowseResultsRenderer")
            ?.jsonObject?.get("tabs")
            ?.jsonArray

        val selectedTab = tabs?.firstOrNull {
            it.jsonObject["tabRenderer"]
                ?.jsonObject?.get("selected")
                ?.jsonPrimitive?.booleanOrNull == true
        }?.jsonObject?.get("tabRenderer")?.jsonObject

        val sectionList = selectedTab?.get("content")
            ?.jsonObject?.get("sectionListRenderer")
            ?.jsonObject?.get("contents")
            ?.jsonArray

        sectionList?.forEach { section ->
            val renderer = section.jsonObject["gridRenderer"]?.jsonObject ?: return@forEach

            renderer["items"]?.jsonArray?.forEach { item ->
                val playlistRenderer = item.jsonObject["musicTwoRowItemRenderer"]?.jsonObject
                    ?: return@forEach

                val title = playlistRenderer["title"]
                    ?.jsonObject?.get("runs")
                    ?.jsonArray?.getOrNull(0)
                    ?.jsonObject?.get("text")
                    ?.jsonPrimitive?.contentOrNull ?: return@forEach

                val browseId = playlistRenderer["navigationEndpoint"]
                    ?.jsonObject?.get("browseEndpoint")
                    ?.jsonObject?.get("browseId")
                    ?.jsonPrimitive?.contentOrNull ?: return@forEach

                val thumbnailUrl =
                    getBestThumbnailUrl(playlistRenderer["thumbnailRenderer"] ?: return@forEach)

                playlistInfos.add(
                    PlaylistInfo(id = browseId, title = title, coverHref = thumbnailUrl)
                )
            }

            val continuationToken = renderer["continuations"]
                ?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("nextContinuationData")
                ?.jsonObject?.get("continuation")
                ?.jsonPrimitive?.contentOrNull

            if (continuationToken != null) {
                val continuationJson = YoutubeRequestHelper.requestContinuation(
                    continuationToken = continuationToken,
                    settings = settings
                )
                playlistInfos.addAll(extractPlaylists(continuationJson, settings))
            }
        }

        val continuationGridItems = json["continuationContents"]
            ?.jsonObject
            ?.get("gridContinuation")
            ?.jsonObject
            ?.get("items")
            ?.jsonArray

        continuationGridItems?.forEach { item ->
            val playlistRenderer = item.jsonObject["musicTwoRowItemRenderer"]?.jsonObject
                ?: return@forEach

            val title = playlistRenderer["title"]
                ?.jsonObject?.get("runs")
                ?.jsonArray?.getOrNull(0)
                ?.jsonObject?.get("text")
                ?.jsonPrimitive?.contentOrNull ?: return@forEach

            val browseId = playlistRenderer["navigationEndpoint"]
                ?.jsonObject?.get("browseEndpoint")
                ?.jsonObject?.get("browseId")
                ?.jsonPrimitive?.contentOrNull ?: return@forEach

            val thumbnailUrl =
                getBestThumbnailUrl(playlistRenderer["thumbnailRenderer"] ?: return@forEach)

            playlistInfos.add(
                PlaylistInfo(id = browseId, title = title, coverHref = thumbnailUrl)
            )
        }

        val continuationToken = json["continuationContents"]
            ?.jsonObject
            ?.get("gridContinuation")
            ?.jsonObject
            ?.get("continuations")
            ?.jsonArray?.firstOrNull()
            ?.jsonObject
            ?.get("nextContinuationData")
            ?.jsonObject
            ?.get("continuation")
            ?.jsonPrimitive?.contentOrNull

        if (continuationToken != null) {
            val continuationJson = YoutubeRequestHelper.requestContinuation(
                continuationToken = continuationToken,
                settings = settings
            )
            playlistInfos.addAll(extractPlaylists(continuationJson, settings))
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


    fun extractSongInfo(jsonString: String): Song {
        val json = Json.parseToJsonElement(jsonString).jsonObject
        val details = json.jsonObject["videoDetails"]?.jsonObject

        val videoId = details?.get("videoId")?.jsonPrimitive?.contentOrNull ?: ""
        val title = details?.get("title")?.jsonPrimitive?.contentOrNull ?: ""
        val author = details?.get("author")?.jsonPrimitive?.contentOrNull ?: ""
        val lengthSeconds: Int =
            details?.get("lengthSeconds")?.jsonPrimitive?.contentOrNull?.toInt()
                ?: 0

        return Song(
            youtubeId = videoId,
            title = title,
            artist = author,
            duration = formatSecondsForYouTubeDisplay(lengthSeconds),
            thumbnailHref = extractHighQualityThumbnail(jsonString)
        )
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


    private fun formatSecondsForYouTubeDisplay(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%d:%02d", minutes, seconds)
        }
    }

    private fun extractHighQualityThumbnail(jsonString: String): String {
        val json = Json.parseToJsonElement(jsonString).jsonObject
        val url = json["videoDetails"]
            ?.jsonObject?.get("thumbnail")
            ?.jsonObject?.get("thumbnails")
            ?.jsonArray?.last()
            ?.jsonObject?.get("url")
            ?.jsonPrimitive?.contentOrNull

        return url ?: ""
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
        } catch (_: Exception) {
            return@withContext false
        }
    }

}


enum class SongInfoType(val index: Int) {
    TITLE(0),
    ARTIST(1),
}