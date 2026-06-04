package ca.ilianokokoro.umihi.music.core.helpers

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.UmihiHttpClient
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.Privacy
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.models.UmihiSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object YoutubeRequestHelper {
    suspend fun browse(browseId: String, settings: UmihiSettings): String {
        return requestWithContext(
            url = Constants.YoutubeApi.Browse.URL,
            idName = "browseId",
            id = browseId,
            settings = settings
        )
    }

    suspend fun requestContinuation(continuationToken: String, settings: UmihiSettings): String {
        return requestWithContext(
            url = Constants.YoutubeApi.Browse.URL,
            idName = "continuation",
            id = continuationToken,
            settings = settings
        )
    }


    suspend fun createPlaylist(
        title: String,
        description: String,
        privacy: Privacy,
        songs: List<Song> = listOf(),
        settings: UmihiSettings
    ): String {
        val baseBody = YoutubeAuthHelper.buildContextBody(
            idName = null,
            id = null,
            settings = settings
        )

        val body = buildJsonObject {
            baseBody.forEach { (key, value) ->
                put(key, value)
            }

            put("title", title)
            put("description", description)
            put("privacyStatus", privacy.value)

            put(
                "videoIds",
                buildJsonArray {
                    songs.forEach { it.youtubeId }
                }
            )
        }

        return requestWithBody(
            url = Constants.YoutubeApi.Create.URL,
            body = body,
            settings = settings
        )
    }

    suspend fun deletePlaylist(
        playlist: PlaylistInfo,
        settings: UmihiSettings
    ): String {
        val baseBody = YoutubeAuthHelper.buildContextBody(
            idName = null,
            id = null,
            settings = settings
        )
        
        val playlistId = playlist.id.removePrefix("VL")

        val body = buildJsonObject {
            baseBody.forEach { (key, value) ->
                put(key, value)
            }

            put("playlistId", playlistId)
        }


        return requestWithBody(
            url = Constants.YoutubeApi.Delete.URL,
            body = body,
            settings = settings
        )
    }


    suspend fun getPlayerInfo(
        videoId: String,
        client: JsonObject? = null,
        visitorData: String? = null
    ): String {
        return requestWithContext(
            url = Constants.YoutubeApi.PlayerInfo.URL,
            idName = "videoId",
            id = videoId,
            client = client,
            visitorData = visitorData
        )
    }

    suspend fun search(query: String): String {
        return requestWithContext(
            url = Constants.YoutubeApi.Search.URL,
            idName = "query",
            id = query
        )
    }

    private suspend fun requestWithBody(
        url: String,
        body: Any,
        settings: UmihiSettings? = null,
        client: JsonObject? = null,
        visitorData: String? = null
    ): String = withContext(Dispatchers.IO) {
        val headers = if (settings != null) {
            YoutubeAuthHelper.getHeaders(settings.cookies, visitorData, client)
        } else if (visitorData != null) {
            mapOf(
                "X-Goog-Visitor-Id" to visitorData
            )
        } else {
            mapOf()
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()

        val requestBody = body
            .toString()
            .toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .apply {
                headers.forEach { (name, value) ->
                    addHeader(name, value)
                }
            }
            .build()

        UmihiHttpClient.client
            .newCall(request)
            .execute()
            .use { response ->
                if (!response.isSuccessful) {
                    throw IOException("HTTP ${response.code}: ${response.message}")
                }

                response.body?.string()
                    ?: throw IOException("Empty response body")
            }
    }

    private suspend fun requestWithContext(
        url: String,
        idName: String,
        id: String,
        settings: UmihiSettings? = null,
        client: JsonObject? = null,
        visitorData: String? = null
    ): String {
        val body = YoutubeAuthHelper.buildContextBody(
            idName,
            id,
            settings,
            client,
            visitorData
        )

        return requestWithBody(
            url = url,
            body = body,
            settings = settings,
            client = client,
            visitorData = visitorData
        )
    }
}