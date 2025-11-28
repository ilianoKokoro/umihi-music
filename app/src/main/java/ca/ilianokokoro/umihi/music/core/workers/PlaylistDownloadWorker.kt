package ca.ilianokokoro.umihi.music.core.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printe
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.models.Playlist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File
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
                            //  val url = YoutubeHelper.getSongPlayerUrl(appContext, song.youtubeId)
                            //  Log.d("PlaylistDownloadWorker", "Got url for ${song.youtubeId}: $url")

                            val thumbnailPath =
                                downloadImage(appContext, song.thumbnailHref, song.youtubeId)
                            val updatedSong = song.copy(thumbnailPath = thumbnailPath?.path)
                            songRepository.create(updatedSong)
                        }
                    }
                }.awaitAll()

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
                val imageDir = File(context.filesDir, Constants.Downloads.THUMBNAILS_FOLDER)
                imageDir.mkdirs()

                val imageFile = File(imageDir, "$id.jpg")

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

    companion object {
        const val PLAYLIST_KEY = "playlist"
    }
}