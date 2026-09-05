package ca.ilianokokoro.umihi.music.core.youtube

import android.content.Context
import android.widget.Toast
import androidx.core.net.toUri
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.UmihiHttpClient
import ca.ilianokokoro.umihi.music.core.helpers.LogHelper.printd
import ca.ilianokokoro.umihi.music.core.helpers.LogHelper.printe
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.safeArray
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.safeObject
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.extensions.getClientName
import ca.ilianokokoro.umihi.music.models.AddToPlaylistOption
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.PlaylistType
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
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Request
import org.schabi.newpipe.extractor.ServiceList
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

var visitorData: String? = null

object YoutubeDataExtractor {
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
        val obj = thumbnailElement.safeObject() ?: return ""

        val fromMusicRenderer = obj["musicThumbnailRenderer"]
            ?.safeObject()
            ?.let { it["thumbnail"] ?: it["thumbnails"] }
            ?.let { getBestThumbnailUrl(it) }
            ?.takeIf { it.isNotBlank() }

        val fromCroppedSquareRenderer = obj["croppedSquareThumbnailRenderer"]
            ?.safeObject()
            ?.let { it["thumbnail"] ?: it["thumbnails"] }
            ?.let { getBestThumbnailUrl(it) }
            ?.takeIf { it.isNotBlank() }

        val fromThumbnailRenderer = obj["thumbnailRenderer"]
            ?.safeObject()
            ?.let { it["thumbnail"] ?: it["thumbnails"] }
            ?.let { getBestThumbnailUrl(it) }
            ?.takeIf { it.isNotBlank() }

        val fromThumbnail = obj["thumbnail"]
            ?.let { getBestThumbnailUrl(it) }
            ?.takeIf { it.isNotBlank() }

        val fromDirectThumbnails = obj["thumbnails"]
            ?.safeArray()?.lastOrNull()
            ?.safeObject()?.get("url")
            ?.jsonPrimitive?.contentOrNull

        return fromMusicRenderer
            ?: fromCroppedSquareRenderer
            ?: fromThumbnailRenderer
            ?: fromThumbnail
            ?: fromDirectThumbnails
            ?: obj["url"]?.jsonPrimitive?.contentOrNull
            ?: ""
    }

    private fun findAnyThumbnailUrl(element: JsonElement?): String? {
        when (element) {
            is JsonObject -> {
                element["url"]?.jsonPrimitive?.contentOrNull
                    ?.takeIf { it.isNotBlank() }
                    ?.let { return it }

                val rendererCandidate = element["musicThumbnailRenderer"]
                    ?: element["croppedSquareThumbnailRenderer"]
                    ?: element["thumbnailRenderer"]
                if (rendererCandidate != null) {
                    getBestThumbnailUrl(rendererCandidate)
                        .takeIf { it.isNotBlank() }
                        ?.let { return it }
                }

                element["thumbnail"]?.let {
                    getBestThumbnailUrl(it).takeIf { url -> url.isNotBlank() }?.let { return it }
                }

                element.forEach { (_, child) ->
                    findAnyThumbnailUrl(child)?.let { return it }
                }
            }

            is JsonArray -> element.forEach {
                findAnyThumbnailUrl(it)?.let { url -> return url }
            }

            else -> Unit
        }
        return null
    }

    fun getSongInfo(songMap: JsonElement, songInfoIndex: SongInfoType): String {
        return songMap.safeObject()?.get("flexColumns")
            ?.safeArray()?.getOrNull(songInfoIndex.index)
            ?.safeObject()?.get("musicResponsiveListItemFlexColumnRenderer")
            ?.safeObject()?.get("text")
            ?.safeObject()?.get("runs")
            ?.safeArray()?.getOrNull(0)
            ?.safeObject()?.get("text")
            ?.jsonPrimitive?.contentOrNull ?: ""
    }

    suspend fun extractPlaylists(
        jsonString: String,
        settings: UmihiSettings
    ): List<PlaylistInfo> {
        val json = Json.parseToJsonElement(jsonString).jsonObject
        val playlistInfos = mutableListOf<PlaylistInfo>()

        suspend fun parseGridRenderer(renderer: JsonObject) {
            renderer["items"]
                ?.safeArray()
                ?.forEach { item ->
                    parsePlaylistItem(item)?.let { playlist ->
                        playlistInfos.add(playlist)
                    }
                }

            val continuationToken = renderer["continuations"]
                ?.safeArray()
                ?.firstOrNull()
                ?.safeObject()
                ?.get("nextContinuationData")
                ?.safeObject()
                ?.get("continuation")
                ?.jsonPrimitive
                ?.contentOrNull

            if (continuationToken != null) {
                val continuationJson = YoutubeApiClient.requestContinuation(
                    continuationToken = continuationToken,
                    settings = settings,
                    //   fields = Constants.YoutubeApi.Browse.Fields.PLAYLISTS_CONTINUATION,
                )
                playlistInfos.addAll(
                    extractPlaylists(
                        jsonString = continuationJson,
                        settings = settings
                    )
                )
            }
        }

        suspend fun parseMusicLibraryRenderer(renderer: JsonObject) {
            renderer["contents"]
                ?.safeArray()
                ?.forEach { item ->
                    parseMusicLibraryItem(item)?.let { playlist ->
                        playlistInfos.add(playlist)
                    }
                }

            val continuationToken = renderer["continuations"]
                ?.safeArray()
                ?.firstOrNull()
                ?.safeObject()
                ?.get("nextContinuationData")
                ?.safeObject()
                ?.get("continuation")
                ?.jsonPrimitive
                ?.contentOrNull

            if (continuationToken != null) {
                val continuationJson = YoutubeApiClient.requestContinuation(
                    continuationToken = continuationToken,
                    settings = settings,
                    //  fields = Constants.YoutubeApi.Browse.Fields.PLAYLISTS_CONTINUATION,
                )
                playlistInfos.addAll(
                    extractPlaylists(
                        jsonString = continuationJson,
                        settings = settings
                    )
                )
            }
        }

        val tabs = json["contents"]
            ?.safeObject()
            ?.get("singleColumnBrowseResultsRenderer")
            ?.safeObject()
            ?.get("tabs")
            ?.safeArray()
            ?: json["contents"]
                ?.safeObject()
                ?.get("twoColumnBrowseResultsRenderer")
                ?.safeObject()
                ?.get("tabs")
                ?.safeArray()

        if (tabs != null) {
            val selectedTab = tabs
                .firstOrNull {
                    it.safeObject()?.get("tabRenderer")
                        ?.safeObject()
                        ?.get("selected")
                        ?.jsonPrimitive
                        ?.booleanOrNull == true
                }
                ?.safeObject()?.get("tabRenderer")?.safeObject()

            val sectionList = selectedTab
                ?.get("content")
                ?.safeObject()
                ?.get("sectionListRenderer")
                ?.safeObject()
                ?.get("contents")
                ?.safeArray()

            if (sectionList != null) {
                for (section in sectionList) {
                    val musicLibrary =
                        section.safeObject()?.get("musicLibraryRenderer")?.safeObject()
                    if (musicLibrary != null) {
                        parseMusicLibraryRenderer(musicLibrary)
                        continue
                    }

                    val gridRenderer = section.safeObject()?.get("gridRenderer")?.safeObject()
                    if (gridRenderer != null) {
                        parseGridRenderer(gridRenderer)
                        continue
                    }

                    val nestedSectionList = section.safeObject()
                        ?.get("sectionListRenderer")?.safeObject()
                        ?.get("contents")?.safeArray()
                    if (nestedSectionList != null) {
                        for (nested in nestedSectionList) {
                            val nestedMusicLibrary =
                                nested.safeObject()?.get("musicLibraryRenderer")?.safeObject()
                            if (nestedMusicLibrary != null) {
                                parseMusicLibraryRenderer(nestedMusicLibrary)
                                continue
                            }
                            val nestedGrid =
                                nested.safeObject()?.get("gridRenderer")?.safeObject()
                            if (nestedGrid != null) {
                                parseGridRenderer(nestedGrid)
                                continue
                            }
                        }
                    }
                }
            } else if (selectedTab != null) {
                val tabContent = selectedTab["content"]?.safeObject()
                val directMusicLibrary = tabContent?.get("musicLibraryRenderer")?.safeObject()
                if (directMusicLibrary != null) {
                    parseMusicLibraryRenderer(directMusicLibrary)
                }
                val directGrid = tabContent?.get("gridRenderer")?.safeObject()
                if (directGrid != null) {
                    parseGridRenderer(directGrid)
                }
                if (directMusicLibrary == null && directGrid == null) {
                    printd("extractPlaylists: tab content has no recognizable renderer. Keys: ${tabContent?.keys}")
                }
            }
        } else {
            printd("extractPlaylists: no tabbed browse result found (may be continuation response)")
        }

        val gridContinuation = json["continuationContents"]
            ?.safeObject()
            ?.get("gridContinuation")
            ?.safeObject()
        if (gridContinuation != null) {
            parseGridRenderer(gridContinuation)
        }

        val musicLibraryContinuation = json["continuationContents"]
            ?.safeObject()
            ?.get("musicLibraryContinuation")
            ?.safeObject()
        if (musicLibraryContinuation != null) {
            parseMusicLibraryRenderer(musicLibraryContinuation)
        }

        if (playlistInfos.isEmpty() && tabs == null && gridContinuation == null && musicLibraryContinuation == null) {
            printd("extractPlaylists: no playlists found in any recognizable structure. Root keys: ${json.keys}")
        }

        return playlistInfos.distinctBy {
            it.id.removePrefix("VL")
        }
    }

    private fun parsePlaylistItem(item: JsonElement): PlaylistInfo? {
        val playlistRenderer = item.safeObject()?.get("musicTwoRowItemRenderer")
            ?.safeObject()
            ?: return null

        val navigationEndpoint = playlistRenderer["navigationEndpoint"]
            ?.safeObject()
            ?: return null

        val isCreatePlaylistTile =
            navigationEndpoint["createPlaylistEndpoint"] != null

        if (isCreatePlaylistTile) {
            return null
        }

        val browseId = navigationEndpoint["browseEndpoint"]
            ?.safeObject()
            ?.get("browseId")
            ?.jsonPrimitive
            ?.contentOrNull
            ?: return null

        val title = playlistRenderer["title"]
            ?.safeObject()
            ?.get("runs")
            ?.safeArray()
            ?.getOrNull(0)
            ?.safeObject()
            ?.get("text")
            ?.jsonPrimitive
            ?.contentOrNull
            ?: return null

        val thumbnailRenderer = playlistRenderer["thumbnailRenderer"]
            ?: return null

        val thumbnailUrl = getBestThumbnailUrl(thumbnailRenderer)

        return PlaylistInfo(
            id = browseId,
            title = title,
            coverHref = thumbnailUrl,
            type = detectPlaylistType(playlistRenderer)
        )
    }

    private fun parseMusicLibraryItem(item: JsonElement): PlaylistInfo? {
        val renderer = item.safeObject()?.get("musicLibraryItemRenderer")
            ?.safeObject()
            ?: return null

        val navigationEndpoint = renderer["navigationEndpoint"]
            ?.safeObject()
            ?: return null

        val browseId = navigationEndpoint["browseEndpoint"]
            ?.safeObject()
            ?.get("browseId")
            ?.jsonPrimitive
            ?.contentOrNull
            ?: return null

        val title = renderer["title"]
            ?.safeObject()
            ?.get("runs")
            ?.safeArray()
            ?.getOrNull(0)
            ?.safeObject()
            ?.get("text")
            ?.jsonPrimitive
            ?.contentOrNull
            ?: return null

        val thumbnailRenderer = renderer["thumbnailRenderer"]
            ?: return null

        val thumbnailUrl = getBestThumbnailUrl(thumbnailRenderer)

        return PlaylistInfo(
            id = browseId,
            title = title,
            coverHref = thumbnailUrl,
            type = detectPlaylistType(renderer)
        )
    }


    private fun detectPlaylistType(renderer: JsonObject): PlaylistType {
        val menuItems = renderer["menu"]
            ?.safeObject()
            ?.get("menuRenderer")
            ?.safeObject()
            ?.get("items")
            ?.safeArray()
            ?: return PlaylistType.AUTOGENERATED

        var saved = false
        for (item in menuItems) {
            val itemObject = item.safeObject() ?: continue

            val navigationRenderer = itemObject["menuNavigationItemRenderer"]?.safeObject()
            val navigationEndpoint = navigationRenderer?.get("navigationEndpoint")?.safeObject()

            if (navigationEndpoint?.get("playlistEditorEndpoint") != null ||
                navigationEndpoint?.get("playlistDeleteEndpoint") != null
            ) {
                return PlaylistType.CREATED_BY_USER
            }

            if (isLibrarySaveToggle(itemObject["toggleMenuServiceItemRenderer"]?.safeObject())) {
                saved = true
            }

            val serviceEndpoint = itemObject["menuServiceItemRenderer"]
                ?.safeObject()
                ?.get("serviceEndpoint")
                ?.safeObject()

            val confirmEndpoint = navigationEndpoint
                ?.get("confirmDialogEndpoint")?.safeObject()
                ?.get("confirmEndpoint")?.safeObject()

            if (isLibraryRemoveEndpoint(serviceEndpoint) ||
                isLibraryRemoveEndpoint(navigationEndpoint) ||
                isLibraryRemoveEndpoint(confirmEndpoint)
            ) {
                saved = true
            }

            val iconType =
                (navigationRenderer ?: itemObject["menuServiceItemRenderer"]?.safeObject())
                    ?.get("icon")?.safeObject()
                    ?.get("iconType")?.jsonPrimitive?.contentOrNull
            if (iconType == "LIBRARY_REMOVE") {
                saved = true
            }
        }

        return if (saved) PlaylistType.SAVED else PlaylistType.AUTOGENERATED
    }

    private fun isLibraryRemoveEndpoint(endpoint: JsonObject?): Boolean =
        endpoint?.keys?.any { key ->
            key.contains("Library", ignoreCase = true) && key.contains("Remove", ignoreCase = true)
        } == true

    private fun isLibrarySaveToggle(toggleRenderer: JsonObject?): Boolean {
        if (toggleRenderer?.get("isToggled")?.jsonPrimitive?.booleanOrNull != true) {
            return false
        }

        val hasLikeEndpoint = toggleRenderer["toggledServiceEndpoint"]
            ?.safeObject()?.get("likeEndpoint") != null
        return hasLikeEndpoint || toggleRenderer["toggledText"]
            ?.safeObject()?.get("runs")?.safeArray()
            ?.any { run ->
                run.safeObject()?.get("text")?.jsonPrimitive
                    ?.contentOrNull?.contains("library", ignoreCase = true) == true
            } == true
    }


    private val ADD_TO_PLAYLIST_RENDERER_KEYS: Set<String> = setOf(
        "playlistAddToOptionRenderer",
        "addToPlaylistItemRenderer",
        "musicResponsiveListItemRenderer",
        "musicTwoRowItemRenderer",
    )

    fun extractAddToPlaylistOptions(jsonString: String): List<AddToPlaylistOption> {
        val json = Json.parseToJsonElement(jsonString).jsonObject

        val root = json["contents"]
            ?.safeArray()
            ?.firstNotNullOfOrNull { it.safeObject()?.get("addToPlaylistRenderer") }
            ?: return emptyList()

        val collected = mutableListOf<AddToPlaylistOption>()
        collectAddToPlaylistOptions(root, collected)

        val seen = mutableSetOf<String>()
        return collected
            .filter { !isLikedMusicPlaylist(it.playlistId) }
            .filter { seen.add(it.playlistId) }
    }

    private fun isLikedMusicPlaylist(playlistId: String): Boolean {
        val normalizedId = playlistId.removePrefix("VL")
        return normalizedId == "LM"
    }

    private fun collectAddToPlaylistOptions(value: JsonElement?, sink: MutableList<AddToPlaylistOption>) {
        when (value) {
            is JsonObject -> {
                ADD_TO_PLAYLIST_RENDERER_KEYS.forEach { key ->
                    value[key]?.safeObject()?.let { renderer ->
                        parseAddToPlaylistOption(renderer)?.let { sink.add(it) }
                    }
                }
                value.forEach { (_, child) -> collectAddToPlaylistOptions(child, sink) }
            }

            is JsonArray -> value.forEach { collectAddToPlaylistOptions(it, sink) }

            else -> Unit
        }
    }

    private fun parseAddToPlaylistOption(renderer: JsonObject): AddToPlaylistOption? {
        val playlistId = findAddToPlaylistPlaylistId(renderer) ?: return null
        val title = extractOptionText(
            renderer,
            "title",
            "text",
            "label",
            "primaryText",
            "header",
            "flexColumns",
        ) ?: return null
        if (title == "Create new playlist" || title == "New playlist") {
            return null
        }

        val thumbnail = renderer["thumbnail"]
            ?: renderer["thumbnailRenderer"]
            ?: renderer["foregroundThumbnail"]
            ?: renderer["thumbnails"]

        val thumbnailUrl = getBestThumbnailUrl(thumbnail ?: JsonObject(emptyMap()))
            .ifBlank { findAnyThumbnailUrl(renderer).orEmpty() }
            .takeIf { it.isNotBlank() }

        printd(
            "add-to-playlist option \"$title\" ($playlistId) " +
                "thumbnail=${thumbnailUrl ?: "MISSING"}"
        )

        return AddToPlaylistOption(
            playlistId = playlistId,
            title = title,
            subtitle = extractOptionText(renderer, "subtitle", "secondaryText"),
            containsSelectedVideos = extractAddToPlaylistSelectedState(renderer),
            thumbnailUrl = thumbnailUrl,
        )
    }

    private fun findAddToPlaylistPlaylistId(value: JsonElement?): String? {
        when (value) {
            is JsonObject -> {
                value["playlistId"]?.jsonPrimitive?.contentOrNull?.let { id ->
                    if (id.isNotBlank()) return id
                }
                value["browseId"]?.jsonPrimitive?.contentOrNull?.let { id ->
                    if (id.startsWith("VL") || id.startsWith("PL")) return id
                }
                value.forEach { (_, child) ->
                    findAddToPlaylistPlaylistId(child)?.let { return it }
                }
            }

            is JsonArray -> value.forEach {
                findAddToPlaylistPlaylistId(it)?.let { id -> return id }
            }

            else -> Unit
        }
        return null
    }

    private fun extractOptionText(renderer: JsonObject, vararg keys: String): String? {
        for (key in keys) {
            if (key == "flexColumns") {
                val text = renderer["flexColumns"]
                    ?.safeArray()
                    ?.firstOrNull()
                    ?.safeObject()
                    ?.get("musicResponsiveListItemFlexColumnRenderer")
                    ?.safeObject()
                    ?.get("text")
                extractTextValue(text)?.let { return it }
                continue
            }

            extractTextValue(renderer[key])?.let { return it }
        }
        extractTextValue(renderer["runs"])?.let { return it }
        return null
    }

    private fun extractAddToPlaylistSelectedState(renderer: JsonObject): Boolean {
        listOf("selected", "isSelected", "checked").forEach { key ->
            renderer[key]?.jsonPrimitive?.booleanOrNull?.let { return it }
        }
        renderer["toggled"]?.jsonPrimitive?.booleanOrNull?.let { return it }

        val checkStatus = renderer["checkStatus"]?.jsonPrimitive?.contentOrNull
        if (checkStatus != null) {
            val normalized = checkStatus.uppercase()
            if (normalized.contains("UNCHECK") || normalized.contains("UNSELECTED")) {
                return false
            }
            return normalized.contains("CHECKED") || normalized.contains("SELECTED")
        }

        return renderer["containsSelectedVideos"]
            ?.jsonPrimitive
            ?.contentOrNull == "ALL"
    }

    private fun extractTextValue(element: JsonElement?): String? {
        if (element is JsonArray) {
            return element.mapNotNull { run ->
                run.safeObject()?.get("text")?.jsonPrimitive?.contentOrNull
            }
                .joinToString("")
                .takeIf { it.isNotBlank() }
        }

        val obj = element?.safeObject() ?: return null
        return obj["runs"]
            ?.safeArray()
            ?.mapNotNull { run ->
                run.safeObject()?.get("text")?.jsonPrimitive?.contentOrNull
            }
            ?.joinToString("")
            ?.takeIf { it.isNotBlank() }
            ?: obj["simpleText"]?.jsonPrimitive?.contentOrNull
    }

    fun extractCreatedPlaylist(jsonString: String): PlaylistInfo? {
        val json = Json.parseToJsonElement(jsonString).jsonObject

        val renderer = json["actions"]
            ?.safeArray()
            ?.firstNotNullOfOrNull { action ->
                action.safeObject()?.get("handlePlaylistCreationCommand")
                    ?.safeObject()
                    ?.get("createdPlaylist")
                    ?.safeObject()
                    ?.get("musicTwoRowItemRenderer")
                    ?.safeObject()
            }
            ?: return null

        val title = renderer["title"]
            ?.safeObject()
            ?.get("runs")
            ?.safeArray()
            ?.getOrNull(0)
            ?.safeObject()
            ?.get("text")
            ?.jsonPrimitive
            ?.contentOrNull
            ?: return null

        val browseId = renderer["navigationEndpoint"]
            ?.safeObject()
            ?.get("browseEndpoint")
            ?.safeObject()
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
            coverHref = thumbnailUrl,
            type = PlaylistType.CREATED_BY_USER
        )
    }

    fun extractSearchResults(jsonString: String): List<Song> {
        val json = Json.parseToJsonElement(jsonString).jsonObject

        val tabs = json["contents"]
            ?.safeObject()?.get("tabbedSearchResultsRenderer")
            ?.safeObject()?.get("tabs")
            ?.safeArray() ?: return emptyList()


        val selectedTab = tabs.firstOrNull {
            it.safeObject()?.get("tabRenderer")
                ?.safeObject()?.get("selected")
                ?.jsonPrimitive?.booleanOrNull == true
        }?.safeObject()?.get("tabRenderer")?.safeObject() ?: return emptyList()

        val contents = selectedTab["content"]
            ?.safeObject()?.get("sectionListRenderer")
            ?.safeObject()?.get("contents")
            ?.safeArray() ?: return emptyList()

        val songRendererList =
            contents
                .firstNotNullOfOrNull {
                    it.safeObject()?.get("musicShelfRenderer")
                        ?.safeObject()?.get("contents")
                        ?.safeArray()
                }
                ?: return emptyList()

        return songRendererList.mapNotNull { extractSong(it) }
    }


    fun extractSongInfo(jsonString: String): Song {
        val json = Json.parseToJsonElement(jsonString).jsonObject
        val details = json["videoDetails"]?.safeObject()

        val videoId = details?.get("videoId")?.jsonPrimitive?.contentOrNull ?: ""
        val title = details?.get("title")?.jsonPrimitive?.contentOrNull ?: ""
        val author = details?.get("author")?.jsonPrimitive?.contentOrNull ?: ""
        val lengthSeconds: Int =
            details?.get("lengthSeconds")?.jsonPrimitive?.contentOrNull?.toInt()
                ?: 0

        val isExplicit = json["microformat"]
            ?.safeObject()?.get("microformatDataRenderer")
            ?.safeObject()?.get("familySafe")
            ?.jsonPrimitive?.booleanOrNull
            ?.let { !it }
            ?: false

        return Song(
            youtubeId = videoId,
            title = title,
            artist = author,
            duration = formatSecondsForYouTubeDisplay(lengthSeconds),
            thumbnailHref = extractHighQualityThumbnail(jsonString),
            isExplicit = isExplicit
        )
    }


    suspend fun extractSongList(jsonString: String, settings: UmihiSettings): List<Song> {
        val json = Json.parseToJsonElement(jsonString).jsonObject

        val contents = json["contents"]
            ?.safeObject()?.get("twoColumnBrowseResultsRenderer")
            ?.safeObject()?.get("secondaryContents")
            ?.safeObject()?.get("sectionListRenderer")
            ?.safeObject()?.get("contents")
            ?.safeArray()?.getOrNull(0)
            ?.safeObject()?.get("musicPlaylistShelfRenderer")
            ?.safeObject()?.get("contents")
            ?.safeArray()

        if (contents != null) {
            return parseSongsFromContents(contents, settings)
        }

        val altContents = json["contents"]
            ?.safeObject()?.get("singleColumnBrowseResultsRenderer")
            ?.safeObject()?.get("tabs")
            ?.safeArray()?.firstOrNull()
            ?.safeObject()?.get("tabRenderer")
            ?.safeObject()?.get("content")
            ?.safeObject()?.get("sectionListRenderer")
            ?.safeObject()?.get("contents")
            ?.safeArray()?.firstOrNull()
            ?.safeObject()?.get("musicPlaylistShelfRenderer")
            ?.safeObject()?.get("contents")
            ?.safeArray()

        if (altContents != null) {
            return parseSongsFromContents(altContents, settings)
        }

        printd("extractSongList: could not find playlist contents. Root keys: ${json.keys}")

        return emptyList()
    }

    suspend fun extractContinuationSongs(jsonString: String, settings: UmihiSettings): List<Song> {
        val json = Json.parseToJsonElement(jsonString).jsonObject

        val contents = json["onResponseReceivedActions"]
            ?.safeArray()?.getOrNull(0)
            ?.safeObject()?.get("appendContinuationItemsAction")
            ?.safeObject()?.get("continuationItems")
            ?.safeArray()

        if (contents == null) {
            printd("extractContinuationSongs: no continuationItems found. Keys: ${json.keys}")
        }

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
            ?.safeObject()?.get("thumbnail")
            ?.safeObject()?.get("thumbnails")
            ?.safeArray()?.lastOrNull()
            ?.safeObject()?.get("url")
            ?.jsonPrimitive?.contentOrNull

        return url ?: ""
    }

    private suspend fun parseSongsFromContents(
        contents: JsonArray?,
        settings: UmihiSettings
    ): List<Song> {
        val songs = mutableListOf<Song>()
        if (contents == null) {
            return songs
        }

        for (shelf in contents) {
            val continuationContent = shelf.safeObject()?.get("continuationItemRenderer")

            if (continuationContent != null) {
                val token = continuationContent.safeObject()?.get("continuationEndpoint")
                    ?.safeObject()?.get("continuationCommand")
                    ?.safeObject()?.get("token")
                    ?.jsonPrimitive?.contentOrNull ?: ""

                val otherSongs = extractContinuationSongs(
                    YoutubeApiClient.requestContinuation(
                        continuationToken = token,
                        settings = settings,
                        // fields = Constants.YoutubeApi.Browse.Fields.SONGS_CONTINUATION,
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
            json.safeObject()?.get("musicResponsiveListItemRenderer")?.safeObject() ?: return null
        val thumbnailUrl = getBestThumbnailUrl(songContent["thumbnail"] ?: return null)

        val title = getSongInfo(songContent, SongInfoType.TITLE)
        val artist = getSongInfo(songContent, SongInfoType.ARTIST)
        val videoId = songContent["playlistItemData"]
            ?.safeObject()?.get("videoId")
            ?.jsonPrimitive?.contentOrNull ?: return null

        val setVideoId = songContent["playlistItemData"]
            ?.safeObject()?.get("playlistSetVideoId")
            ?.jsonPrimitive?.contentOrNull
            ?: songContent["playlistItemData"]
                ?.safeObject()?.get("setVideoId")
                ?.jsonPrimitive?.contentOrNull
            ?: extractRemovalSetVideoId(json)


        val duration = extractDuration(songContent)

        val isExplicit = songContent["badges"]
            ?.safeArray()
            ?.any { badge ->
                badge.safeObject()
                    ?.get("musicInlineBadgeRenderer")
                    ?.safeObject()
                    ?.get("icon")
                    ?.safeObject()
                    ?.get("iconType")
                    ?.jsonPrimitive
                    ?.contentOrNull == "MUSIC_EXPLICIT_BADGE"
            } ?: false

        val isLiked = songContent["menu"]
            ?.safeObject()?.get("menuRenderer")
            ?.safeObject()?.get("topLevelButtons")
            ?.safeArray()
            ?.firstOrNull { item ->
                item.safeObject()?.get("likeButtonRenderer") != null
            }
            ?.safeObject()?.get("likeButtonRenderer")
            ?.safeObject()?.get("likeStatus")
            ?.jsonPrimitive?.contentOrNull == "LIKE"

        return Song(
            youtubeId = videoId,
            title = title,
            artist = artist,
            duration = duration,
            thumbnailHref = thumbnailUrl,
            isExplicit = isExplicit,
            isLiked = isLiked,
        ).also { song ->
            song.setVideoId = setVideoId
        }

    }

    private fun extractRemovalSetVideoId(element: JsonElement): String? {
        val jsonObject = element.safeObject() ?: return null
        val actions = jsonObject["playlistEditEndpoint"]
            ?.safeObject()?.get("actions")
            ?.safeArray()
        if (actions != null) {
            for (action in actions) {
                val actionObject = action.safeObject() ?: continue
                if (actionObject.get("action")?.jsonPrimitive?.contentOrNull == "ACTION_REMOVE_VIDEO") {
                    actionObject.get("setVideoId")
                        ?.jsonPrimitive?.contentOrNull?.let { return it }
                }
            }
        }
        for (value in jsonObject.values) {
            extractRemovalSetVideoId(value)?.let { return it }
        }
        return null
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
            Toast.makeText(
                context,
                context.getString(R.string.failed_get_local_song),
                Toast.LENGTH_LONG
            )
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
            ?.safeArray()
            ?.firstOrNull()
            ?.safeObject()
            ?.get("musicResponsiveListItemFixedColumnRenderer")
            ?.safeObject()
            ?.get("text")
            ?.safeObject()
            ?.get("runs")
            ?.safeArray()
            ?.firstOrNull()
            ?.safeObject()
            ?.get("text")
            ?.jsonPrimitive
            ?.contentOrNull

        if (fixedDuration != null) {
            return fixedDuration
        }

        val flexColumns = songContent["flexColumns"]
            ?.safeArray()
            ?: return ""

        for (column in flexColumns) {
            val runs = column.safeObject()?.get("musicResponsiveListItemFlexColumnRenderer")
                ?.safeObject()
                ?.get("text")
                ?.safeObject()
                ?.get("runs")
                ?.safeArray()
                ?: continue

            for (run in runs) {
                val text = run.safeObject()?.get("text")
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

        val fastUrl = resolveAnonymousStreamUrl(song.youtubeId)

        if (fastUrl != null) {
            return fastUrl
        }

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
                    delay((Constants.YoutubeApi.RETRY_DELAY * (attempt + 1)).milliseconds)
                }
            }
        }

        throw Exception(
            "${song.youtubeId} : Fatal fail. Could not get it after $retries attempts",
            lastError
        )
    }

    private suspend fun resolveAnonymousStreamUrl(
        videoId: String,
    ): String? = withContext(Dispatchers.IO) {
        suspend fun executeRequest(client: JsonObject): String {
            val response = YoutubeApiClient.getPlayerInfo(
                videoId = videoId,
                client = client,
                visitorData = visitorData,
            )

            return extractStreamFromRawResponse(response)
        }

        val softCap = Constants.YoutubeApi.SOFT_TRIES_PER_CLIENT
        val hardCap = Constants.YoutubeApi.HARD_TRIES_PER_CLIENT

        var totalRequests = 0

        for (client in Constants.YoutubeApi.Client.FALLBACK_ORDER) {
            val clientName = client.getClientName()
            var hardTries = 0
            var softTries = 0

            while (hardTries < hardCap && softTries < softCap) {
                val previousVisitorData = visitorData

                totalRequests++
                val errorMessage = try {
                    val stream = executeRequest(client)
                    printd(
                        "[$clientName] Resolved stream in $totalRequests total request(s)"
                    )
                    return@withContext stream

                } catch (ex: Exception) {
                    ex.message
                }

                val visitorDataUpdated = previousVisitorData != visitorData
                hardTries++

                if (visitorDataUpdated) {
                    printd(
                        "[$clientName] Updating visitor data"
                    )
                    continue
                }

                softTries++

                printe(
                    "[${clientName}] $errorMessage"
                )
            }
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

    private suspend fun extractStreamFromRawResponse(
        text: String,
    ): String {
        val root = Json.parseToJsonElement(text).jsonObject

        val newVisitorData = root["responseContext"]
            ?.safeObject()
            ?.get("visitorData")
            ?.jsonPrimitive
            ?.contentOrNull

        if (newVisitorData != null && newVisitorData != visitorData) {
            visitorData = newVisitorData
        }

        val status = root["playabilityStatus"]
            ?.safeObject()
            ?.get("status")
            ?.jsonPrimitive
            ?.contentOrNull

        val reason = root["playabilityStatus"]
            ?.safeObject()
            ?.get("reason")
            ?.jsonPrimitive
            ?.contentOrNull

        val directUrl = root["streamingData"]
            ?.safeObject()
            ?.get("adaptiveFormats")
            ?.safeArray()
            ?.asSequence()
            ?.mapNotNull { it.safeObject() }
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

        if (directUrl == null) {
            throw Exception("($status) $reason")
        }

        if (!isYoutubeUrlValid(directUrl)) {
            throw Exception("($status) Url was invalid")
        }

        return directUrl
    }

    private suspend fun isYoutubeUrlValid(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .head()
                .build()

            UmihiHttpClient.client
                .newCall(request)
                .execute()
                .use { response ->
                    response.code in 200..399
                }
        } catch (_: Exception) {
            false
        }
    }
}


enum class SongInfoType(val index: Int) {
    TITLE(0),
    ARTIST(1),
}
