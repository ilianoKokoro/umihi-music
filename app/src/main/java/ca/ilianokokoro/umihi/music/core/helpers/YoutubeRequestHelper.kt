package ca.ilianokokoro.umihi.music.core.helpers

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.models.Cookies
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object YoutubeRequestHelper {
    fun browse(browseId: String, cookies: Cookies): String {
        return requestWithContext(
            url = Constants.YoutubeApi.Browse.URL,
            idName = "browseId",
            id = browseId,
            cookies = cookies
        )
    }

    fun requestContinuation(continuationToken: String, cookies: Cookies): String {
        return requestWithContext(
            url = Constants.YoutubeApi.Browse.URL,
            idName = "continuation",
            id = continuationToken,
            cookies = cookies
        )
    }


    fun getPlayerInfo(videoId: String): String {
        return requestWithContext(
            url = Constants.YoutubeApi.PlayerInfo.URL,
            idName = "videoId",
            id = videoId
        )
    }

    private fun requestWithContext(
        url: String,
        idName: String,
        id: String,
        cookies: Cookies? = null
    ): String {
        val body =
            buildJsonObject {
                put("context", Constants.YoutubeApi.Browse.CLIENT)
                put(idName, id)
            }


        val headers = if (cookies != null) {
            YoutubeAuthHelper.getHeaders(cookies)
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