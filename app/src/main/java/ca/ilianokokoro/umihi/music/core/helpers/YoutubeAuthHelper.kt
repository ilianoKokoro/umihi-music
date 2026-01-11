package ca.ilianokokoro.umihi.music.core.helpers

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.models.Cookies
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.security.MessageDigest
import kotlin.text.Charsets.UTF_8

object YoutubeAuthHelper {
    fun getHeaders(cookies: Cookies): Map<String, Any> {
        val client = Constants.YoutubeApi.Browse.CLIENT["client"]?.jsonObject
        val headers = mutableMapOf(
            "Content-Type" to "application/json",
            "Origin" to Constants.YoutubeApi.ORIGIN,
            "Referer" to "${Constants.YoutubeApi.ORIGIN}/",
            "X-Goog-Api-Format-Version" to 1,
            "X-Origin" to Constants.YoutubeApi.ORIGIN,
            "X-YouTube-Client-Version" to client?.jsonObject["clientVersion"]?.jsonPrimitive.toString(),
            "X-YouTube-Client-Name" to client?.jsonObject["xClientName"]?.jsonPrimitive.toString(),
            "Cookie" to cookies.toRawCookie(),
            "User-Agent" to client?.jsonObject["userAgent"]?.jsonPrimitive.toString()
        )

        val cookieMap = cookies.data
        val sapisidCookie = cookieMap["SAPISID"] ?: cookieMap["__Secure-3PAPISID"]
        if (sapisidCookie != null) {
            headers["Authorization"] = generateSapisidHash(sapisidCookie)
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
