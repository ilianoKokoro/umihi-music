package ca.ilianokokoro.umihi.music.core.helpers

import android.content.Context
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printd
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

object DownloadHelper {
    private val client = OkHttpClient()
    
    suspend fun downloadImage(context: Context, imageUrl: String, id: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val imageDir =
                    UmihiHelper.getDownloadDirectory(context, Constants.Downloads.THUMBNAILS_FOLDER)
                val imageFile = File(imageDir, "$id.jpg")

                if (imageFile.exists()) {
                    printd("Song Image $id was already downloaded")
                    return@withContext imageFile
                }

                URL(imageUrl).openStream().use { input ->
                    imageFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                imageFile

            } catch (e: Exception) {
                printe(
                    tag = "PlaylistDownloadWorker",
                    message = "Error Downloading Thumbnail",
                    exception = e
                )
                null
            }
        }
    }

    suspend fun downloadAudio(
        context: Context,
        youtubeId: String,
        connections: Int = 8
    ): String? = withContext(Dispatchers.IO) {

        val audioDir =
            UmihiHelper.getDownloadDirectory(context, Constants.Downloads.AUDIO_FILES_FOLDER)
        val outputFile = File(audioDir, "$youtubeId.webm")

        if (outputFile.exists()) {
            return@withContext outputFile.absolutePath
        }

        val url = YoutubeHelper.getSongPlayerUrl(context, youtubeId)

        val total = try {
            val headReq = Request.Builder()
                .url(url)
                .header("Range", "bytes=0-0")
                .build()

            client.newCall(headReq).execute().use { headRes ->
                if (!headRes.isSuccessful) {
                    return@withContext null
                }
                headRes.headers["Content-Range"]
                    ?.substringAfter("/")
                    ?.toLongOrNull()
                    ?: return@withContext null
            }
        } catch (e: Exception) {
            printe("Failed to get content length: ${e.message}")
            return@withContext null
        }

        val chunkSize = total / connections
        val tempFiles = mutableListOf<File>()

        try {
            (0 until connections).map { i ->
                async {
                    val start = i * chunkSize
                    val end = if (i == connections - 1) total - 1 else (start + chunkSize - 1)
                    val temp = File(audioDir, "$youtubeId.part$i")

                    try {
                        val req = Request.Builder()
                            .url(url)
                            .header("Range", "bytes=$start-$end")
                            .header("User-Agent", Constants.YoutubeApi.USER_AGENT)
                            .build()

                        client.newCall(req).execute().use { response ->
                            if (!response.isSuccessful) {
                                throw IOException("Failed to download chunk $i: ${response.code}")
                            }

                            response.body?.byteStream()?.use { input ->
                                FileOutputStream(temp).use { output ->
                                    input.copyTo(output)
                                }
                            } ?: throw IOException("Empty response body for chunk $i")
                        }

                        temp
                    } catch (e: Exception) {
                        temp.delete()
                        throw e
                    }
                }
            }.awaitAll().also { tempFiles.addAll(it) }

            FileOutputStream(outputFile).use { out ->
                tempFiles.sortedBy { it.name }.forEach { part ->
                    part.inputStream().use { it.copyTo(out) }
                    part.delete()
                }
            }

            return@withContext outputFile.absolutePath

        } catch (e: Exception) {
            printe("Download failed for $youtubeId: ${e.message}")
            tempFiles.forEach { it.delete() }
            outputFile.delete()
            return@withContext null
        }
    }
}