package ca.ilianokokoro.umihi.music.core.youtube

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.models.UmihiSettings
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl
import okhttp3.Request
import okio.ByteString.Companion.encodeUtf8
import kotlin.time.Clock

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
        val origin = "${url.scheme}://${url.host}"

        val clientToUse = client ?: Constants.YoutubeApi.Client.WEB_REMIX

        val now = Clock.System.now()
        val nowMs = now.toEpochMilliseconds()
        val tz = TimeZone.currentSystemDefault()
        val utcOffsetMinutes = tz.offsetAt(now).totalSeconds / 60

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


        settings?.cookies?.let {
            headers["Cookie"] = it.toRawCookie()

            val cookieMap = it.data
            val sapisidCookie = cookieMap["SAPISID"] ?: cookieMap["__Secure-3PAPISID"]

            if (sapisidCookie != null) {
                headers["Authorization"] = generateSapisidHash(sapisidCookie, origin)
            }
        }

        return headers
    }

    private fun generateSapisidHash(sapisidCookie: String, origin: String): String {
        val currentTime = Clock.System.now().epochSeconds
        val sapisidHash = sha1("$currentTime $sapisidCookie $origin")
        val fullAuthToken = "${currentTime}_$sapisidHash"
        return "SAPISIDHASH $fullAuthToken SAPISID1PHASH $fullAuthToken SAPISID3PHASH $fullAuthToken"
    }

    private fun sha1(input: String): String {
        return input.encodeUtf8().sha1().hex()
    }
}
