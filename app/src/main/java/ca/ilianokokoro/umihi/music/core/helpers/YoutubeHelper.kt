package ca.ilianokokoro.umihi.music.core.helpers

import android.content.Context
import android.widget.Toast
import androidx.core.net.toUri
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.UmihiHttpClient
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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.schabi.newpipe.extractor.ServiceList
import java.util.Locale

object YoutubeHelper {
    private var visitorData: String? = null // TODO use safer and more global value

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

    suspend fun extractPlaylists(
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


    fun extractCreatedPlaylist(jsonString: String): PlaylistInfo? {
        val json = Json.parseToJsonElement(jsonString).jsonObject

        val renderer = json["actions"]
            ?.jsonArray
            ?.firstNotNullOfOrNull { action ->
                action.jsonObject["handlePlaylistCreationCommand"]
                    ?.jsonObject
                    ?.get("createdPlaylist")
                    ?.jsonObject
                    ?.get("musicTwoRowItemRenderer")
                    ?.jsonObject
            }
            ?: return null

        val title = renderer["title"]
            ?.jsonObject
            ?.get("runs")
            ?.jsonArray
            ?.getOrNull(0)
            ?.jsonObject
            ?.get("text")
            ?.jsonPrimitive
            ?.contentOrNull
            ?: return null

        val browseId = renderer["navigationEndpoint"]
            ?.jsonObject
            ?.get("browseEndpoint")
            ?.jsonObject
            ?.get("browseId")
            ?.jsonPrimitive
            ?.contentOrNull
            ?: return null

        val thumbnailUrl = getBestThumbnailUrl(
            renderer["thumbnailRenderer"] ?: return null
        )

        return PlaylistInfo(
            id = browseId,
            title = title,
            coverHref = thumbnailUrl
        )
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
            contents.jsonArray
                .firstNotNullOfOrNull {
                    it.jsonObject["musicShelfRenderer"]
                        ?.jsonObject?.get("contents")
                        ?.jsonArray
                }
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


    suspend fun extractSongList(jsonString: String, settings: UmihiSettings): List<Song> {
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

    suspend fun extractContinuationSongs(jsonString: String, settings: UmihiSettings): List<Song> {
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

    private suspend fun parseSongsFromContents(
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


        val duration = extractDuration(songContent)

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
        song: Song,
        allowLocal: Boolean = false
    ): String {
        val localSongRepository = AppDatabase.getInstance(context).songRepository()
        var savedSong: Song? = null
        try {
            savedSong = localSongRepository.getSong(song.youtubeId)
        } catch (ex: Exception) {
            Toast.makeText(context, "Failed to get song from local repository", Toast.LENGTH_LONG)
                .show()
            printe(ex.toString())
        }

        if (savedSong != null) {
            if (allowLocal && savedSong.audioFilePath != null) {
                printd("${song.youtubeId} : Was downloaded")
                return savedSong.audioFilePath
            }

            if (savedSong.streamUrl != null) {
                if (isYoutubeUrlValid(savedSong.streamUrl)) {
                    printd("${song.youtubeId} : Got url from saved")
                    return savedSong.streamUrl
                }
                printd("${song.youtubeId} : Saved url was invalid")
            }
        }

        val newUri = getSongUrlFromYoutube(song)
        localSongRepository.setStreamUrl(songId = song.youtubeId, streamUrl = newUri)
        printd("${song.youtubeId} : Got url from YouTube and saved song")
        return newUri
    }


    private fun extractDuration(songContent: JsonObject): String {
        val durationRegex = Regex("""\d+:\d{2}(:\d{2})?""")

        val fixedDuration = songContent["fixedColumns"]
            ?.jsonArray
            ?.firstOrNull()
            ?.jsonObject
            ?.get("musicResponsiveListItemFixedColumnRenderer")
            ?.jsonObject
            ?.get("text")
            ?.jsonObject
            ?.get("runs")
            ?.jsonArray
            ?.firstOrNull()
            ?.jsonObject
            ?.get("text")
            ?.jsonPrimitive
            ?.contentOrNull

        if (fixedDuration != null) {
            return fixedDuration
        }

        val flexColumns = songContent["flexColumns"]
            ?.jsonArray
            ?: return ""

        for (column in flexColumns) {
            val runs = column.jsonObject["musicResponsiveListItemFlexColumnRenderer"]
                ?.jsonObject
                ?.get("text")
                ?.jsonObject
                ?.get("runs")
                ?.jsonArray
                ?: continue

            for (run in runs) {
                val text = run.jsonObject["text"]
                    ?.jsonPrimitive
                    ?.contentOrNull
                    ?: continue

                if (durationRegex.matches(text)) {
                    return text
                }
            }
        }

        return ""
    }

    private suspend fun getSongUrlFromYoutube(
        song: Song,
        retries: Int = Constants.YoutubeApi.RETRY_COUNT
    ): String {
        var lastError: Throwable? = null

        val fastUrl = resolveAndroidVrStreamUrl(song.youtubeId)

        if (fastUrl != null) return fastUrl

        printd("${song.youtubeId} : Falling back to NewPipe")
        repeat(retries) { attempt ->
            try {
                return withContext(Dispatchers.IO) {
                    resolveNewPipeStreamUrl(song)
                }
            } catch (e: Throwable) {
                lastError = e

                printe(
                    "${song.youtubeId} : Failed attempt ${attempt + 1}/$retries with NewPipe: " +
                            "${e::class.simpleName}: ${e.message ?: "no message"}"
                )

                if (attempt < retries - 1) {
                    delay(Constants.YoutubeApi.RETRY_DELAY * (attempt + 1))
                }
            }
        }

        throw Exception(
            "${song.youtubeId} : Fatal fail. Could not get it after $retries attempts",
            lastError
        )
    }

    private suspend fun resolveAndroidVrStreamUrl(
        videoId: String,
        retries: Int = Constants.YoutubeApi.RETRY_COUNT,
    ): String? = withContext(Dispatchers.IO) {
        suspend fun executeRequest(): String? {
            val response = YoutubeRequestHelper.getPlayerInfo(
                videoId = videoId,
                client = Constants.YoutubeApi.Client.ANDROID_VR,
                visitorData = visitorData,
            )

            return extractStreamFromAndroidVrResponse(response)
        }

        repeat(retries) { attempt ->
            val previousVisitorData = visitorData

            executeRequest()?.let {
                return@withContext it
            }

            val visitorDataUpdated =
                previousVisitorData != visitorData

            val isLastAttempt =
                attempt >= retries - 1

            if (!visitorDataUpdated || isLastAttempt) {
                return@repeat
            }

            printd(
                "Retrying ANDROID_VR with updated visitorData " +
                        "(${attempt + 1}/$retries)"
            )
        }

        null
    }

    private fun resolveNewPipeStreamUrl(song: Song): String {
        val service = ServiceList.YouTube
        val extractor = service.getStreamExtractor(song.youtubeUrl)

        extractor.fetchPage()

        val bestAudioStream = extractor.audioStreams
            .filter { it.content.isNotBlank() }
            .maxByOrNull { it.averageBitrate }
            ?: error("No valid audio streams found")

        return bestAudioStream.content
    }

    private fun extractStreamFromAndroidVrResponse(
        text: String,
    ): String? {
        val root = Json.parseToJsonElement(text).jsonObject

        val visitorData = root["responseContext"]
            ?.jsonObject
            ?.get("visitorData")
            ?.jsonPrimitive
            ?.contentOrNull

        if (YoutubeHelper.visitorData == null) {
            YoutubeHelper.visitorData = visitorData
        }

        val status = root["playabilityStatus"]
            ?.jsonObject
            ?.get("status")
            ?.jsonPrimitive
            ?.contentOrNull

        val reason = root["playabilityStatus"]
            ?.jsonObject
            ?.get("reason")
            ?.jsonPrimitive
            ?.contentOrNull

        val directUrl = root["streamingData"]
            ?.jsonObject
            ?.get("adaptiveFormats")
            ?.jsonArray
            ?.asSequence()
            ?.map { it.jsonObject }
            ?.filter {
                it["url"]?.jsonPrimitive?.contentOrNull?.isNotBlank() == true
            }
            ?.filter {
                it["mimeType"]
                    ?.jsonPrimitive
                    ?.contentOrNull
                    ?.startsWith("audio/", ignoreCase = true) == true
            }
            ?.maxByOrNull {
                it["bitrate"]?.jsonPrimitive?.intOrNull ?: 0
            }
            ?.get("url")
            ?.jsonPrimitive
            ?.contentOrNull

        if (reason != null) {
            printe("ANDROID_VR Failed ($status). Reason : $reason ")
        }

        return directUrl
    }

    private suspend fun isYoutubeUrlValid(url: String): Boolean = withContext(Dispatchers.IO) {
        try {

            val result = UmihiHttpClient.fuelClient.head(request = { this.url = url })

            result.statusCode in 200..399
        } catch (_: Exception) {
            false
        }
    }
}


enum class SongInfoType(val index: Int) {
    TITLE(0),
    ARTIST(1),
}