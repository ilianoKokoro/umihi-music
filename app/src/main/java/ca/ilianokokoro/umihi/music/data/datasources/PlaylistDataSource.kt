package ca.ilianokokoro.umihi.music.data.datasources

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.AuthHelper
import ca.ilianokokoro.umihi.music.core.helpers.YoutubeHelper
import ca.ilianokokoro.umihi.music.models.Cookies
import ca.ilianokokoro.umihi.music.models.Playlist
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class PlaylistDataSource {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun retrieveAll(cookies: Cookies): List<Playlist> {
        val url = "${Constants.YoutubeApi.Browse.URL}?alt=json&key=${Constants.YoutubeApi.KEY}"

        val body = json.encodeToString(
            BrowseBody(
                context = Context(
                    client = Client(
                        clientName = "WEB_REMIX", clientVersion = "1.20250212.01.00"
                    )

                ),
                browseId = Constants.YoutubeApi.Browse.PLAYLIST_ID
            )
        )
        // TODO

        val headers = AuthHelper.getHeaders(cookies)
        val (_, _, result) = url.httpPost().jsonBody(body)
            .header(
                headers
            )
            .responseJson()

        return when (result) {
            is Result.Success -> {
                extractPlaylistsFromRawJson(result.value.content)
            }

            is Result.Failure -> {
                throw result.error.exception
            }
        }
    }

    private fun extractPlaylistsFromRawJson(jsonString: String): List<Playlist> {
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

}


@Serializable
data class BrowseBody(
    val context: Context,
    val browseId: String
)

@Serializable
data class Context(
    val client: Client
)

@Serializable
data class Client(
    val clientName: String,
    val clientVersion: String
)