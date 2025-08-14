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
        val body =
            buildJsonObject {
                put("context", Constants.YoutubeApi.Browse.CONTEXT)
                put("browseId", browseId)
            }


        val headers = YoutubeAuthHelper.getHeaders(cookies)
        val (_, _, result) = Constants.YoutubeApi.Browse.URL.httpPost().jsonBody(body.toString())
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


    fun getPlayer(videoId: String, cookies: Cookies): String {
        val body =
            buildJsonObject {
                put("context", Constants.YoutubeApi.Player.CONTEXT)
                put("videoId", videoId)
            }


        val headers = YoutubeAuthHelper.getHeaders(cookies)
        val (_, _, result) = Constants.YoutubeApi.Player.URL.httpPost().jsonBody(body.toString())
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