package ca.ilianokokoro.umihi.music.core.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ca.ilianokokoro.umihi.music.core.helpers.YoutubeHelper
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.models.Playlist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlaylistDownloadWorker(
    private val appContext: Context,
    private val params: WorkerParameters
) :
    CoroutineWorker(appContext, params) {
    private val localRepository = AppDatabase.getInstance(appContext).playlistRepository()

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("PlaylistDownloadWorker", "Starting work")
                val playlistId = params.inputData.getString(PLAYLIST_KEY)!!
                val playlist: Playlist = localRepository.getPlaylistById(playlistId)!!

                for (song in playlist.songs) {
                    val url = YoutubeHelper.getSongPlayerUrl(appContext, song.id)
                    Log.d("PlaylistDownloadWorker", "Got url : $url")
                }
                Result.success()
            } catch (e: Exception) {
                Log.e("PlaylistDownloadWorker", "Error Downloading Playlist", e)
                Result.failure()
            }
        }
    }

    companion object {
        const val PLAYLIST_KEY = "playlist"
    }
}