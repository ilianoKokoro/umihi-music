package ca.ilianokokoro.umihi.music.core

import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.io.IOException

internal class YoutubeExtractor : Downloader() {

    private val cookieStore = HashMap<String, String>()

    @Throws(IOException::class, ReCaptchaException::class)
    override fun execute(request: Request): Response {
        val url = request.url()
        val requestBody: RequestBody? = request.dataToSend()?.toRequestBody()

        val requestBuilder = okhttp3.Request.Builder()
            .url(url)
            .method(request.httpMethod(), requestBody)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "*/*")
            .header("Accept-Language", "en-US,en;q=0.9")
            .header("Origin", YOUTUBE_HOST)
            .header("Referer", "$YOUTUBE_HOST/")

        val cookieHeader = getCookies()
        if (cookieHeader.isNotBlank()) {
            requestBuilder.header("Cookie", cookieHeader)
        }

        for ((headerName, headerValueList) in request.headers()) {
            requestBuilder.removeHeader(headerName)

            for (headerValue in headerValueList) {
                requestBuilder.addHeader(headerName, headerValue)
            }
        }

        UmihiHttpClient.client.newCall(requestBuilder.build()).execute().use { response ->
            if (response.code == 429) {
                throw ReCaptchaException("YouTube rate limit / reCAPTCHA challenge", url)
            }

            val responseBody = response.body?.string()
            val latestUrl = response.request.url.toString()

            return Response(
                response.code,
                response.message,
                response.headers.toMultimap(),
                responseBody,
                latestUrl
            )
        }
    }

    private fun getCookies(): String {
        val resultCookies = mutableListOf<String>()

        getCookie(RECAPTCHA_COOKIES_KEY)?.let(resultCookies::add)

        return concatCookies(resultCookies)
    }

    private fun getCookie(key: String): String? {
        return cookieStore[key]
    }

    private fun concatCookies(cookieStrings: Collection<String>): String {
        return cookieStrings
            .flatMap { splitCookies(it) }
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString("; ")
    }

    private fun splitCookies(cookies: String): List<String> {
        return cookies
            .split(";")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:140.0) Gecko/20100101 Firefox/140.0"
        const val YOUTUBE_HOST = "https://www.youtube.com"
        const val RECAPTCHA_COOKIES_KEY = "recaptcha_cookies"
    }


}