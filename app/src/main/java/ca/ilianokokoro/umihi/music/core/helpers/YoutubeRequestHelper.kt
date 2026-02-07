package ca.ilianokokoro.umihi.music.core.helpers

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.models.UmihiSettings
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result

object YoutubeRequestHelper {
    fun browse(browseId: String, settings: UmihiSettings): String {
        return requestWithContext(
            url = Constants.YoutubeApi.Browse.URL,
            idName = "browseId",
            id = browseId,
            settings = settings
        )
    }

    fun requestContinuation(continuationToken: String, settings: UmihiSettings): String {
        return requestWithContext(
            url = Constants.YoutubeApi.Browse.URL,
            idName = "continuation",
            id = continuationToken,
            settings = settings
        )
    }

    fun getPlayerInfo(videoId: String): String {
        return requestWithContext(
            url = Constants.YoutubeApi.PlayerInfo.URL,
            idName = "videoId",
            id = videoId
        )
    }

    fun search(query: String): String {
        return requestWithContext(
            url = Constants.YoutubeApi.Search.URL,
            idName = "query",
            id = query
        )
    }

    private fun requestWithContext(
        url: String,
        idName: String,
        id: String,
        settings: UmihiSettings? = null
    ): String {
        val body =
            YoutubeAuthHelper.buildContextBody(idName, id, settings)

        val headers = if (settings != null) {
            YoutubeAuthHelper.getHeaders(settings.cookies)
        } else {
            mapOf()
        }

        val (_, _, result) = url.httpPost().jsonBody(body.toString())
            .header(
                headers
            )
            .responseJson()

        return when (result) {
            is Result.Success -> {
                result.value.content
            }

            is Result.Failure -> {
                throw result.error.exception
            }
        }
    }
}