package ca.ilianokokoro.umihi.music.core.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.UmihiHttpClient
import ca.ilianokokoro.umihi.music.extensions.cappedTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Paths
import kotlin.time.measureTimedValue


object UmihiHelper {
    const val TAG = "UmihiPrint"

    inline fun <T> benchmark(
        label: String,
        block: () -> T
    ): T {
        val result = measureTimedValue(block)
        printd(tag = "UmihiBench", message = "$label: ${result.duration.inWholeMilliseconds} ms")
        return result.value
    }

    fun printd(message: String, tag: String = TAG) {
        Log.d(tag, message)
    }

    fun printe(message: String, tag: String = TAG, exception: Exception? = null) {
        Log.e(TAG, message, exception)
    }

    fun getDownloadDirectory(context: Context, directory: String? = null): File {
        val dir = File(
            context.filesDir,
            if (directory == null)
                Constants.Downloads.DIRECTORY
            else
                Paths.get(Constants.Downloads.DIRECTORY, directory)
                    .toString()
        )
        dir.mkdirs()
        return dir
    }


    suspend fun fetchArtworkBytes(url: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                val bytes = UmihiHttpClient.client
                    .newCall(request)
                    .execute()
                    .use { response ->
                        if (!response.isSuccessful) {
                            throw IllegalStateException(
                                "Failed to fetch artwork. HTTP ${response.code}: ${response.message}"
                            )
                        }

                        response.body?.bytes()
                            ?: throw IllegalStateException("Empty artwork response body")
                    }

                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    ?: return@withContext null

                ByteArrayOutputStream().use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                    stream.toByteArray().cappedTo()
                }
            } catch (e: Exception) {
                Log.e("Artwork", "Failed to fetch artwork: ${e.message}", e)
                null
            }
        }
    }

    fun Float.speedLabel(): String {
        val value = String.format("%.2f", this)
            .trimEnd('0')
            .trimEnd('.')

        return "${value}x"
    }

    fun JsonElement.safeObject(): JsonObject? =
        try {
            this.jsonObject
        } catch (_: Exception) {
            null
        }

    fun JsonElement.safeArray(): JsonArray? =
        try {
            this.jsonArray
        } catch (_: Exception) {
            null
        }

}
