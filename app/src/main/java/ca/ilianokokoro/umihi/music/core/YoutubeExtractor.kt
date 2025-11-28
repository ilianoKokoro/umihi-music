package ca.ilianokokoro.umihi.music.core

import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.io.IOException

internal class YoutubeExtractor(private val client: OkHttpClient) : Downloader() {
    private val cookies = HashMap<String, String>()

    private fun getCookies(url: String): String {
        val resultCookies: MutableList<String> = ArrayList()
        if (url.contains(YOUTUBE_DOMAIN)) {
            val youtubeCookie = getCookie(YOUTUBE_RESTRICTED_MODE_COOKIE_KEY)
            if (youtubeCookie != null) {
                resultCookies.add(youtubeCookie)
            }
        }
        val recaptchaCookie = getCookie(RECAPTCHA_COOKIES_KEY)
        if (recaptchaCookie != null) {
            resultCookies.add(recaptchaCookie)
        }
        return concatCookies(resultCookies)
    }

    private fun getCookie(key: String): String? {
        return cookies[key]
    }

    @Throws(IOException::class, ReCaptchaException::class)
    override fun execute(request: Request): Response {
        val httpMethod = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()
        val requestBody: RequestBody? = dataToSend?.toRequestBody()

        val requestBuilder = okhttp3.Request.Builder()
            .method(httpMethod, requestBody).url(url)
            .addHeader("User-Agent", USER_AGENT)
        val cookies = getCookies(url)
        if (cookies.isNotEmpty()) {
            requestBuilder.addHeader("Cookie", cookies)
        }
        for ((headerName, headerValueList) in headers) {
            if (headerValueList.size > 1) {
                requestBuilder.removeHeader(headerName)
                for (headerValue in headerValueList) {
                    requestBuilder.addHeader(headerName, headerValue)
                }
            } else if (headerValueList.size == 1) {
                requestBuilder.header(headerName, headerValueList[0])
            }
        }
        val response = client.newCall(requestBuilder.build()).execute()
        if (response.code == 429) {
            response.close()
            throw ReCaptchaException("reCaptcha Challenge requested", url)
        }
        val body = response.body
        val responseBodyToReturn = body?.string()
        val latestUrl = response.request.url.toString()
        return Response(
            response.code, response.message, response.headers.toMultimap(),
            responseBodyToReturn, latestUrl
        )
    }

    private fun concatCookies(cookieStrings: Collection<String>): String {
        val cookieSet: MutableSet<String?> = HashSet()
        for (cookies in cookieStrings) {
            cookieSet.addAll(splitCookies(cookies))
        }
        return cookieSet.joinToString("; ").trim(' ')
    }

    private fun splitCookies(cookies: String): Set<String?> {
        return HashSet(listOf(*cookies.split("; *".toRegex()).toTypedArray()))
    }

    companion object {
        const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; rv:78.0) Gecko/20100101 Firefox/78.0"
        const val YOUTUBE_RESTRICTED_MODE_COOKIE_KEY = "youtube_restricted_mode_key"
        const val YOUTUBE_DOMAIN = "youtube.com"
        const val RECAPTCHA_COOKIES_KEY = "recaptcha_cookies"
    }

}