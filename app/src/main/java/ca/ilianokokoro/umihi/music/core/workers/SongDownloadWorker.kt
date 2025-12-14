package ca.ilianokokoro.umihi.music.core.workers

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.helpers.DownloadHelper
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printd
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printe
import ca.ilianokokoro.umihi.music.core.managers.UmihiNotificationManager
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.data.repositories.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import kotlin.coroutines.cancellation.CancellationException

class SongDownloadWorker(
    private val appContext: Context,
    private val params: WorkerParameters
) :
    CoroutineWorker(appContext, params) {

    private val playlistRepository = AppDatabase.getInstance(appContext).playlistRepository()
    private val localSongRepository = AppDatabase.getInstance(appContext).songRepository()
    private val songRepository = SongRepository()

    @OptIn(UnstableApi::class)
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val playlistId = params.inputData.getString(PLAYLIST_KEY)
                ?: return@withContext Result.failure()

            val songId = params.inputData.getString(SONG_KEY)
                ?: return@withContext Result.failure()

            val playlist = playlistRepository.getPlaylistById(playlistId)
                ?: return@withContext Result.failure()

            val song = localSongRepository.getSong(songId)
                ?: return@withContext Result.failure()


            val playlistImage =
                DownloadHelper.downloadImage(
                    appContext,
                    playlist.info.coverHref,
                    playlist.info.id
                )
            playlistRepository.insertPlaylist(
                playlist.info.copy(
                    coverPath = playlistImage?.path
                )
            )

            try {
                var result: ApiResult<String>? = null
                songRepository.getSongThumbnail(song.youtubeId)
                    .collect { r ->
                        when (r) {
                            is ApiResult.Loading -> {}
                            else -> {
                                result = r
                                return@collect
                            }
                        }
                    }

                val audioPath =
                    DownloadHelper.downloadAudio(
                        appContext, song.youtubeId,
                    )
                val thumbnailPath =
                    (result as? ApiResult.Success)
                        ?.data
                        ?.let {
                            DownloadHelper.downloadImage(
                                appContext,
                                it,
                                song.youtubeId
                            )
                        }

                val updatedSong = song.copy(
                    thumbnailPath = thumbnailPath?.path,
                    audioFilePath = audioPath,
                )
                UmihiNotificationManager.showSongDownloadSuccess(appContext, song)
                localSongRepository.create(updatedSong)
                Result.success()
            } catch (e: CancellationException) {
                printd("Song download canceled ${song.title}")
                Result.failure()
            } catch (e: Exception) {
                UmihiNotificationManager.showSongDownloadFailed(
                    appContext,
                    song
                )
                printe(
                    message = "Error downloading song: ${song.youtubeId}",
                    exception = e
                )
                Result.failure()
            }
        }
    }

    companion object {
        const val PLAYLIST_KEY = "playlist"
        const val SONG_KEY = "song"
        private val client = OkHttpClient()
    }
}
