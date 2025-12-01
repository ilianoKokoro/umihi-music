package ca.ilianokokoro.umihi.music.core.workers

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printd
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printe
import ca.ilianokokoro.umihi.music.core.helpers.YoutubeHelper
import ca.ilianokokoro.umihi.music.core.managers.UmihiNotificationManager
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class PlaylistDownloadWorker(
    private val appContext: Context,
    private val params: WorkerParameters
) :
    CoroutineWorker(appContext, params) {

    private val playlistRepository = AppDatabase.getInstance(appContext).playlistRepository()
    private val songRepository = AppDatabase.getInstance(appContext).songRepository()

    @OptIn(UnstableApi::class)
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val playlistId = params.inputData.getString(PLAYLIST_KEY)
                ?: return@withContext Result.failure()

            val playlist = playlistRepository.getPlaylistById(playlistId)
                ?: return@withContext Result.failure()

            try {
                val totalSongs = playlist.songs.size
                var downloadedSongs = 0

                UmihiNotificationManager.showPlaylistDownloadProgress(
                    appContext,
                    playlist,
                    0,
                    totalSongs
                )

                val semaphore = Semaphore(Constants.Downloads.MAX_CONCURRENT_DOWNLOADS)
                val playlistImage =
                    downloadImage(appContext, playlist.info.coverHref, playlist.info.id)
                playlistRepository.insertPlaylist(
                    playlist.info.copy(
                        coverPath = playlistImage?.path
                    )
                )

                playlist.songs.map { song ->
                    async {
                        semaphore.withPermit {
                            try {
                                val thumbnailPath = downloadImage(
                                    appContext,
                                    song.thumbnailHref,
                                    song.youtubeId
                                )
                                val audioPath = downloadAudio(appContext, song.youtubeId)

                                val updatedSong = song.copy(
                                    thumbnailPath = thumbnailPath?.path,
                                    audioFilePath = audioPath,
                                )
                                songRepository.create(updatedSong)

                                UmihiNotificationManager.showPlaylistDownloadProgress(
                                    appContext,
                                    playlist,
                                    ++downloadedSongs,
                                    totalSongs
                                )
                            } catch (e: Exception) {
                                UmihiNotificationManager.showSongDownloadFailed(
                                    appContext,
                                    song
                                )
                                printe(
                                    message = "Error downloading song: ${song.youtubeId}",
                                    exception = e
                                )
                            }
                        }
                    }
                }.awaitAll()


                UmihiNotificationManager.showPlaylistDownloadSuccess(appContext, playlist)
                printd("Playlist download complete")
                delay(Constants.Downloads.DEBOUNCE_DELAY)
                Result.success()
            } catch (e: Exception) {
                UmihiNotificationManager.showPlaylistDownloadFailure(appContext, playlist)
                printe(message = e.toString(), exception = e)
                delay(Constants.Downloads.DEBOUNCE_DELAY)
                Result.failure()
            }
        }
    }

    private suspend fun downloadImage(context: Context, imageUrl: String, id: String): File? {
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

        if (outputFile.exists()) return@withContext outputFile.absolutePath

        val url = YoutubeHelper.getSongPlayerUrl(appContext, youtubeId)

        val headReq = Request.Builder()
            .url(url)
            .header("Range", "bytes=0-0")
            .build()

        val headRes = client.newCall(headReq).execute()
        val total = headRes.headers["Content-Range"]
            ?.substringAfter("/")
            ?.toLongOrNull()
            ?: return@withContext null

        val chunkSize = total / connections
        val tempFiles = mutableListOf<File>()

        (0 until connections).map { i ->
            async {
                val start = i * chunkSize
                val end = if (i == connections - 1) total - 1 else (start + chunkSize - 1)
                val temp = File(audioDir, "$youtubeId.part$i")

                val req = Request.Builder()
                    .url(url)
                    .header("Range", "bytes=$start-$end")
                    .header("User-Agent", Constants.YoutubeApi.USER_AGENT)
                    .build()

                client.newCall(req).execute().body?.byteStream().use { input ->
                    FileOutputStream(temp).use { output -> input?.copyTo(output) }
                }
                tempFiles += temp
            }
        }.awaitAll()

        FileOutputStream(outputFile).use { out ->
            tempFiles.sortedBy { it.name }.forEach { part ->
                part.inputStream().use { it.copyTo(out) }
                part.delete()
            }
        }

        return@withContext outputFile.absolutePath
    }

    companion object {
        const val PLAYLIST_KEY = "playlist"
        private val client = OkHttpClient()
    }


}