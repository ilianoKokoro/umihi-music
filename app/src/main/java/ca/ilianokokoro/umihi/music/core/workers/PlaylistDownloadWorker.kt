package ca.ilianokokoro.umihi.music.core.workers

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.DownloadHelper
import ca.ilianokokoro.umihi.music.core.helpers.LogHelper.printd
import ca.ilianokokoro.umihi.music.core.helpers.LogHelper.printe
import ca.ilianokokoro.umihi.music.core.managers.NotificationManager
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

            NotificationManager.showPlaylistDownloadProgress(
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

                                val downloaded = downloadedSongs.incrementAndGet()
                                if (downloaded < totalSongs) {
                                    NotificationManager.showPlaylistDownloadProgress(
                                        appContext,
                                        playlist,
                                        downloaded,
                                        totalSongs
                                    )
                                }
                            } catch (e: CancellationException) {
                                printd("Song download canceled ${song.title}")
                                throw e
                            } catch (e: Exception) {
                                NotificationManager.showSongDownloadFailed(
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

            NotificationManager.showPlaylistDownloadSuccess(appContext, playlist)
            printd("Playlist download complete")

            Result.success()
        } catch (_: CancellationException) {
            NotificationManager.showPlaylistDownloadCanceled(appContext, playlist)
            printd("Playlist download canceled ${playlist.info.title}")
            Result.failure()
        } catch (e: Exception) {
            NotificationManager.showPlaylistDownloadFailure(appContext, playlist)
            printe(message = e.toString(), exception = e)
            Result.failure()
        }
    }


    companion object {
        const val PLAYLIST_KEY = "playlist"
    }
}