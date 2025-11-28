package ca.ilianokokoro.umihi.music.core.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printd
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printe
import ca.ilianokokoro.umihi.music.core.helpers.YoutubeHelper
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.models.Playlist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val playlistId = params.inputData.getString(PLAYLIST_KEY)!!
                val playlist: Playlist = playlistRepository.getPlaylistById(playlistId)!!

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
                            val thumbnailPath =
                                downloadImage(appContext, song.thumbnailHref, song.youtubeId)
                            val audioPath = downloadAudio(appContext, song.youtubeId)
                            val updatedSong = song.copy(
                                thumbnailPath = thumbnailPath?.path,
                                audioFilePath = audioPath,
                            )
                            songRepository.create(updatedSong)
                        }
                    }
                }.awaitAll()

                printd("Playlist download complete")
                Result.success()
            } catch (e: Exception) {
                printe(
                    tag = "PlaylistDownloadWorker",
                    message = "Error Downloading Playlist",
                    exception = e
                )
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
    ): String? = withContext(Dispatchers.IO) {
        try {
            val audioDir = UmihiHelper.getDownloadDirectory(
                context = context,
                directory = Constants.Downloads.AUDIO_FILES_FOLDER
            )
            val outputFile = File(audioDir, "$youtubeId.webm")
            if (outputFile.exists()) {
                printd("Song Audio $id was already downloaded")
                return@withContext null
            }

            val url = YoutubeHelper.getSongPlayerUrl(appContext, youtubeId)
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) return@withContext null
            response.body?.byteStream()?.use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }

            return@withContext outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    companion object {
        private const val PLAYLIST_KEY = "playlist"
        private val client = OkHttpClient()
    }


}