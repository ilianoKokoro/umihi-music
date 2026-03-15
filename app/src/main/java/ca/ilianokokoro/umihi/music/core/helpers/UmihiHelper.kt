package ca.ilianokokoro.umihi.music.core.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.extensions.cappedTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Paths


object UmihiHelper {
    const val TAG = "UmihiPrint"

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
                val client = OkHttpClient()

                val request = Request.Builder()
                    .url(url)
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) return@withContext null

                val bytes = response.body?.bytes() ?: return@withContext null

                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    ?: return@withContext null

                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                stream.toByteArray().cappedTo()
            } catch (e: Exception) {
                Log.e("Artwork", "Failed to fetch artwork: ${e.message}")
                null
            }
        }
    }
}
