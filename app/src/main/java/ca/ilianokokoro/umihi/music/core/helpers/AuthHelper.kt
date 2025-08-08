package ca.ilianokokoro.umihi.music.core.helpers

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.models.Cookies
import java.security.MessageDigest
import kotlin.text.Charsets.UTF_8

object AuthHelper {
    fun getHeaders(cookies: Cookies): Map<String, String> {
        val timestamp = System.currentTimeMillis() / 1000

        val sapisid = cookies.data["SAPISID"]

        val sapisidHash = generateSapisidHash(sapisid ?: "", Constants.YoutubeApi.ORIGIN, timestamp)

        return mapOf(
            "Content-Type" to "application/json",
            "Origin" to Constants.YoutubeApi.ORIGIN,
            "Referer" to "${Constants.YoutubeApi.ORIGIN}/",
            "X-Goog-Api-Format-Version" to "1",
            "Authorization" to "SAPISIDHASH ${timestamp}_$sapisidHash",
            "Cookie" to cookies.toRawCookie(),
            "User-Agent" to Constants.YoutubeApi.USER_AGENT
        )
    }

    fun generateSapisidHash(sapisid: String, origin: String, timestamp: Long): String {
        val input = "$timestamp $sapisid $origin"
        val sha1 = MessageDigest.getInstance("SHA-1")
        val hashBytes = sha1.digest(input.toByteArray(UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
