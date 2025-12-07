package ca.ilianokokoro.umihi.music.data.repositories

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printd
import ca.ilianokokoro.umihi.music.core.workers.PlaylistDownloadWorker
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.models.Playlist
import kotlinx.coroutines.flow.Flow
import java.io.File

class DownloadRepository(appContext: Context) {
    private val _appContext = appContext
    private val workManager: WorkManager = WorkManager.getInstance(_appContext)
    private val localRepository =
        AppDatabase.getInstance(_appContext).playlistRepository()
    private val localPlaylistRepository = AppDatabase.getInstance(_appContext).playlistRepository()

    suspend fun download(playlist: Playlist) {
        val existingWork = getExistingJobs(playlist)
        if (existingWork.isNotEmpty()) {
            printd("Download is already ongoing for playlist ${playlist.info.title}")
            return
        }

        localRepository.insertPlaylistWithSongs(playlist)
        val request = OneTimeWorkRequestBuilder<PlaylistDownloadWorker>().setInputData(
            workDataOf(
                PlaylistDownloadWorker.PLAYLIST_KEY to playlist.info.id
            )
        ).setConstraints(
            Constraints(
                requiredNetworkType = NetworkType.CONNECTED,
                requiresStorageNotLow = true
            )
        ).build()


        workManager.enqueueUniqueWork(playlist.info.id, ExistingWorkPolicy.KEEP, request)
    }

    suspend fun delete(playlist: Playlist) {
        localPlaylistRepository.deleteFullPlaylist(playlist.info.id)

        playlist.songs.forEach { song ->
            val audioDir =
                UmihiHelper.getDownloadDirectory(
                    _appContext,
                    Constants.Downloads.AUDIO_FILES_FOLDER
                )
            val outputFile = File(audioDir, "${song.youtubeId}.webm")

            if (outputFile.exists()) {
                outputFile.delete()
            }

            val imageDir =
                UmihiHelper.getDownloadDirectory(_appContext, Constants.Downloads.THUMBNAILS_FOLDER)
            val imageFile = File(imageDir, "${song.youtubeId}.jpg")

            if (imageFile.exists()) {
                imageFile.delete()
            }
        }
    }


    fun getExistingJobFlow(playlist: Playlist): Flow<List<WorkInfo>> {
        return workManager.getWorkInfosForUniqueWorkFlow(playlist.info.id)

    }

    private fun getExistingJobs(playlist: Playlist): List<WorkInfo> {
        return workManager.getWorkInfosForUniqueWork(playlist.info.id).get().filter {
            it.state == WorkInfo.State.ENQUEUED ||
                    it.state == WorkInfo.State.RUNNING ||
                    it.state == WorkInfo.State.BLOCKED
        }
    }
}