package ca.ilianokokoro.umihi.music.core.helpers

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.models.Cookies
import ca.ilianokokoro.umihi.music.models.UmihiSettings
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.security.MessageDigest
import kotlin.text.Charsets.UTF_8

object YoutubeAuthHelper {

    fun buildContextBody(
        idName: String?,
        id: String?,
        settings: UmihiSettings?,
        client: JsonObject? = null,
        visitorData: String? = null,
    ): JsonObject {
        val clientToUse = client ?: Constants.YoutubeApi.Client.WEB_REMIX

        return buildJsonObject {
            val user = buildJsonObject {
                put("lockedSafetyMode", JsonPrimitive(false))

                if (!settings?.dataSyncId.isNullOrBlank()) {
                    put("onBehalfOfUser", JsonPrimitive(settings.dataSyncId))
                }

            }

            val context = buildJsonObject {
                put("client", clientToUse)
                put("user", user)

                visitorData?.let {
                    put("visitorData", JsonPrimitive(it))
                }
            }

            put("context", context)

            if (idName != null) {
                put(idName, JsonPrimitive(id))

                if (idName == "query") {
                    put("params", JsonPrimitive(Constants.YoutubeApi.Search.FILTER))
                }
            }
        }
    }

    fun getHeaders(
        cookies: Cookies? = null,
        visitorData: String? = null,
        client: JsonObject? = null
    ): Map<String, String> {
        val clientToUse = client ?: Constants.YoutubeApi.Client.WEB_REMIX

        val headers = mutableMapOf(
            "Content-Type" to "application/json; charset=utf-8",
            "Origin" to Constants.YoutubeApi.ORIGIN,
            "Referer" to "${Constants.YoutubeApi.ORIGIN}/",
            "X-Goog-Api-Format-Version" to "1",
            "X-Origin" to Constants.YoutubeApi.ORIGIN,
        )

        clientToUse["clientVersion"]
            ?.jsonPrimitive
            ?.contentOrNull
            ?.let {
                headers["X-YouTube-Client-Version"] = it
            }

        clientToUse["xClientName"]
            ?.jsonPrimitive
            ?.contentOrNull
            ?.let {
                headers["X-YouTube-Client-Name"] = it
            }

        clientToUse["userAgent"]
            ?.jsonPrimitive
            ?.contentOrNull
            ?.let {
                headers["User-Agent"] = it
            }

        visitorData?.let {
            headers["X-Goog-Visitor-Id"] = it
        }

        if (cookies != null) {
            headers["Cookie"] = cookies.toRawCookie()

            val cookieMap = cookies.data
            val sapisidCookie = cookieMap["SAPISID"] ?: cookieMap["__Secure-3PAPISID"]

            if (sapisidCookie != null) {
                headers["Authorization"] = generateSapisidHash(sapisidCookie)
            }
        }

        return headers
    }

    private fun generateSapisidHash(sapisidCookie: String): String {
        val currentTime = System.currentTimeMillis() / 1000
        val sapisidHash = sha1("$currentTime $sapisidCookie ${Constants.YoutubeApi.ORIGIN}")
        val fullAuthToken = "${currentTime}_$sapisidHash"
        return "SAPISIDHASH $fullAuthToken SAPISID1PHASH $fullAuthToken SAPISID3PHASH $fullAuthToken"
    }

    private fun sha1(input: String): String {
        val sha1 = MessageDigest.getInstance("SHA-1")
        val hashBytes = sha1.digest(input.toByteArray(UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
