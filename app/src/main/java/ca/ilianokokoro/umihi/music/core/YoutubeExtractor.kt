package ca.ilianokokoro.umihi.music.core

import ca.ilianokokoro.umihi.music.core.youtube.YoutubeAuthHelper.applyHeaders
import okhttp3.HttpUrl.Companion.toHttpUrl
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
        val url = request.url().toHttpUrl()
        val requestBody: RequestBody? = request.dataToSend()?.toRequestBody()

        val requestBuilder = okhttp3.Request.Builder()
            .url(url)
            .method(request.httpMethod(), requestBody)
            .applyHeaders(url = url, settings = null)

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
                throw ReCaptchaException("YouTube rate limit / reCAPTCHA challenge", url.toString())
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
        const val RECAPTCHA_COOKIES_KEY = "recaptcha_cookies"
    }


}