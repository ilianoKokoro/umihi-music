package ca.ilianokokoro.umihi.music.core.youtube

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.models.UmihiSettings
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl
import okhttp3.Request
import java.security.MessageDigest
import java.util.TimeZone
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

                settings?.dataSyncId?.let {
                    put("onBehalfOfUser", JsonPrimitive(it))
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

    fun Request.Builder.applyHeaders(
        url: HttpUrl,
        settings: UmihiSettings?,
        visitorData: String? = null,
        client: JsonObject? = null,
    ) = apply {
        getHeaders(url, settings, visitorData, client).forEach { (name, value) ->
            addHeader(name, value)
        }
    }

    private fun getHeaders(
        url: HttpUrl,
        settings: UmihiSettings? = null,
        visitorData: String? = null,
        client: JsonObject? = null,
    ): Map<String, String> {
        if (settings == null) {
            return if (visitorData == null) {
                mapOf()
            } else {
                mapOf(
                    "X-Goog-Visitor-Id" to visitorData
                )
            }
        }


        val origin = "${url.scheme}://${url.host}"
        val cookies = settings.cookies

        val clientToUse = client ?: Constants.YoutubeApi.Client.WEB_REMIX

        val nowMs = System.currentTimeMillis()
        val tz = TimeZone.getDefault()
        val utcOffsetMinutes = tz.getOffset(nowMs) / 60000

        val headers = mutableMapOf(
            "Content-Type" to "application/json; charset=utf-8",
            "Origin" to origin,
            "Referer" to "$origin/",
            "X-Goog-Api-Format-Version" to "1",
            "X-Origin" to origin,
            "X-Goog-Event-Time" to nowMs.toString(),
            "X-Goog-Request-Time" to nowMs.toString(),
            "X-YouTube-Utc-Offset" to utcOffsetMinutes.toString(),
            "X-YouTube-Time-Zone" to tz.id
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
                headers["Authorization"] = generateSapisidHash(sapisidCookie, origin)
            }
        }

        return headers
    }

    private fun generateSapisidHash(sapisidCookie: String, origin: String): String {
        val currentTime = System.currentTimeMillis() / 1000
        val sapisidHash = sha1("$currentTime $sapisidCookie $origin")
        val fullAuthToken = "${currentTime}_$sapisidHash"
        return "SAPISIDHASH $fullAuthToken SAPISID1PHASH $fullAuthToken SAPISID3PHASH $fullAuthToken"
    }

    private fun sha1(input: String): String {
        val sha1 = MessageDigest.getInstance("SHA-1")
        val hashBytes = sha1.digest(input.toByteArray(UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
