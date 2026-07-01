package ca.ilianokokoro.umihi.music.core.helpers

import android.content.Context
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.UmihiHttpClient
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printd
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printe
import ca.ilianokokoro.umihi.music.models.Song
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object DownloadHelper {

    suspend fun downloadImage(context: Context, imageUrl: String, id: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val imageDir = UmihiHelper.getDownloadDirectory(
                    context,
                    Constants.Downloads.THUMBNAILS_FOLDER
                )

                val imageFile = File(imageDir, "$id.jpg")

                if (imageFile.exists()) {
                    printd("Song Image $id was already downloaded")
                    return@withContext imageFile
                }

                val tempFile = File(imageDir, "$id.jpg.part")

                if (tempFile.exists()) {
                    tempFile.delete()
                }

                val request = Request.Builder()
                    .url(imageUrl)
                    .get()
                    .build()

                UmihiHttpClient.client
                    .newCall(request)
                    .execute()
                    .use { response ->
                        if (!response.isSuccessful) {
                            throw IOException("HTTP ${response.code}: ${response.message}")
                        }

                        val body = response.body ?: throw IOException("Empty image response body")

                        body.byteStream().use { input ->
                            tempFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }

                if (!tempFile.renameTo(imageFile)) {
                    throw IOException("Failed to rename thumbnail temp file")
                }

                imageFile
            } catch (e: CancellationException) {
                throw e
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
        song: Song,
        retries: Int = Constants.YoutubeApi.RETRY_COUNT
    ): String? = withContext(Dispatchers.IO) {
        val audioDir = UmihiHelper.getDownloadDirectory(
            context,
            Constants.Downloads.AUDIO_FILES_FOLDER
        )

        val outputFile = File(audioDir, "${song.youtubeId}.webm")
        val tempFile = File(audioDir, "${song.youtubeId}.webm.part")

        if (outputFile.exists()) {
            printd("Song file ${song.title} was already downloaded")
            return@withContext outputFile.absolutePath
        }

        val url = YoutubeHelper.getSongPlayerUrl(context, song)

        var lastException: Exception? = null

        repeat(retries) { attempt ->
            try {
                if (tempFile.exists()) {
                    tempFile.delete()
                }

                val request = Request.Builder()
                    .url(url)
                    .header("Range", "bytes=0-")
                    .build()

                UmihiHttpClient.downloadClient
                    .newCall(request)
                    .execute()
                    .use { response ->
                        if (!response.isSuccessful) {
                            throw IOException("Failed to download audio: ${response.code}")
                        }

                        val body = response.body
                            ?: throw IOException("Empty audio response body")

                        body.byteStream().use { input ->
                            FileOutputStream(tempFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }

                if (outputFile.exists()) {
                    outputFile.delete()
                }

                if (!tempFile.renameTo(outputFile)) {
                    throw IOException("Failed to rename temp audio file")
                }

                return@withContext outputFile.absolutePath
            } catch (e: CancellationException) {
                tempFile.delete()
                throw e
            } catch (e: Exception) {
                tempFile.delete()
                lastException = e

                if (attempt == retries - 1) {
                    throw e
                }
            }
        }

        printe(
            message = "Download failed for ${song.youtubeId}: ${lastException?.message}",
            exception = lastException
        )

        tempFile.delete()
        outputFile.delete()
        null
    }
}