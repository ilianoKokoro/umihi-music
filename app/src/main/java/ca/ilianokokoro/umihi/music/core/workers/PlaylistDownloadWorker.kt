package ca.ilianokokoro.umihi.music.core.workers

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.DownloadHelper
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printd
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printe
import ca.ilianokokoro.umihi.music.core.managers.UmihiNotificationManager
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.data.repositories.SongRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.cancellation.CancellationException

class PlaylistDownloadWorker(
    private val appContext: Context,
    private val params: WorkerParameters
) :
    CoroutineWorker(appContext, params) {

    private val playlistRepository = AppDatabase.getInstance(appContext).playlistRepository()
    private val localSongRepository = AppDatabase.getInstance(appContext).songRepository()
    private val songRepository = SongRepository()

    @OptIn(UnstableApi::class)
    override suspend fun doWork(): Result {
        val playlistId = params.inputData.getString(PLAYLIST_KEY)
            ?: return Result.failure()

        val playlist = playlistRepository.getPlaylistById(playlistId)
            ?: return Result.failure()

        return try {
            val totalSongs = playlist.songs.size
            val downloadedSongs = AtomicInteger(0)

            UmihiNotificationManager.showPlaylistDownloadProgress(
                appContext,
                playlist,
                0,
                totalSongs
            )

            val playlistImage = DownloadHelper.downloadImage(
                appContext,
                playlist.info.coverHref,
                playlist.info.id
            )

            playlistRepository.insertPlaylist(
                playlist.info.copy(
                    coverPath = playlistImage?.path
                )
            )

            val semaphore = Semaphore(Constants.Downloads.MAX_CONCURRENT_DOWNLOADS)

            coroutineScope {
                playlist.songs.map { song ->
                    async {
                        semaphore.withPermit {
                            try {
                                val fullSongData = songRepository
                                    .getSongInfo(song.youtubeId)
                                    .first { it is ApiResult.Success }

                                val fullSong = (fullSongData as ApiResult.Success).data

                                val audioPath = DownloadHelper.downloadAudio(appContext, song)

                                val thumbnailPath = DownloadHelper.downloadImage(
                                    appContext,
                                    fullSong.thumbnailHref,
                                    song.youtubeId
                                )

                                val updatedSong = song.copy(
                                    thumbnailPath = thumbnailPath?.path,
                                    audioFilePath = audioPath,
                                )

                                localSongRepository.create(updatedSong)

                                UmihiNotificationManager.showPlaylistDownloadProgress(
                                    appContext,
                                    playlist,
                                    downloadedSongs.incrementAndGet(),
                                    totalSongs
                                )
                            } catch (e: CancellationException) {
                                printd("Song download canceled ${song.title}")
                                throw e
                            } catch (e: Exception) {
                                UmihiNotificationManager.showSongDownloadFailed(
                                    appContext,
                                    song
                                )

                                printe(
                                    message = "Error downloading song: ${song.title}",
                                    exception = e
                                )
                            }
                        }
                    }
                }.awaitAll()
            }

            UmihiNotificationManager.showPlaylistDownloadSuccess(appContext, playlist)
            printd("Playlist download complete")

            Result.success()
        } catch (_: CancellationException) {
            UmihiNotificationManager.showPlaylistDownloadCanceled(appContext, playlist)
            printd("Playlist download canceled ${playlist.info.title}")
            Result.failure()
        } catch (e: Exception) {
            UmihiNotificationManager.showPlaylistDownloadFailure(appContext, playlist)
            printe(message = e.toString(), exception = e)
            Result.failure()
        }
    }


    companion object {
        const val PLAYLIST_KEY = "playlist"
    }
}