package ca.ilianokokoro.umihi.music.core.workers

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.helpers.DownloadHelper
import ca.ilianokokoro.umihi.music.core.helpers.LogHelper.printd
import ca.ilianokokoro.umihi.music.core.helpers.LogHelper.printe
import ca.ilianokokoro.umihi.music.core.managers.NotificationManager
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.data.repositories.SongRepository
import kotlinx.coroutines.flow.first
import kotlin.coroutines.cancellation.CancellationException

class SongDownloadWorker(
    private val appContext: Context,
    private val params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val playlistRepository = AppDatabase.getInstance(appContext).playlistRepository()
    private val localSongRepository = AppDatabase.getInstance(appContext).songRepository()
    private val songRepository = SongRepository()

    @OptIn(UnstableApi::class)
    override suspend fun doWork(): Result {
        val playlistId = params.inputData.getString(PLAYLIST_KEY)
            ?: return Result.failure()

        val songId = params.inputData.getString(SONG_KEY)
            ?: return Result.failure()

        val playlist = playlistRepository.getPlaylistById(playlistId)
            ?: return Result.failure()

        val song = localSongRepository.getSong(songId)
            ?: return Result.failure()

        return try {
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

            NotificationManager.showSongDownloadSuccess(appContext, song)

            Result.success()
        } catch (_: CancellationException) {
            printd("Song download canceled ${song.title}")
            Result.failure()
        } catch (e: Exception) {
            NotificationManager.showSongDownloadFailed(appContext, song)

            printe(
                message = "Error downloading song: ${song.youtubeId}",
                exception = e
            )

            Result.failure()
        }
    }

    companion object {
        const val PLAYLIST_KEY = "playlist"
        const val SONG_KEY = "song"
    }
}