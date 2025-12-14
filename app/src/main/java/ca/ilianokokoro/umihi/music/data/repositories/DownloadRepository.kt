package ca.ilianokokoro.umihi.music.data.repositories

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printd
import ca.ilianokokoro.umihi.music.core.workers.PlaylistDownloadWorker
import ca.ilianokokoro.umihi.music.core.workers.SongDownloadWorker
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.Song
import kotlinx.coroutines.flow.Flow
import java.io.File

class DownloadRepository(appContext: Context) {
    private val _appContext = appContext
    private val workManager: WorkManager = WorkManager.getInstance(_appContext)
    private val localPlaylistRepository = AppDatabase.getInstance(_appContext).playlistRepository()
    private val localSongRepository = AppDatabase.getInstance(_appContext).songRepository()

    suspend fun downloadPlaylist(playlist: Playlist) {
        val existingWork = getExistingJobs(playlist.info.id)
        if (existingWork.isNotEmpty()) {
            printd("Download is already ongoing for playlist ${playlist.info.title}")
            return
        }
        localPlaylistRepository.insertPlaylistWithSongs(playlist)
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

    suspend fun deletePlaylist(playlist: Playlist) {
        localPlaylistRepository.deleteFullPlaylist(playlist.info.id)
        val audioDir =
            UmihiHelper.getDownloadDirectory(
                _appContext,
                Constants.Downloads.AUDIO_FILES_FOLDER
            )
        val imageDir =
            UmihiHelper.getDownloadDirectory(_appContext, Constants.Downloads.THUMBNAILS_FOLDER)

        File(
            imageDir,
            _appContext.getString(R.string.jpg_extension, playlist.info.id)
        ).takeIf { it.exists() }?.delete()

        val songIds = playlist.songs.map { it.youtubeId }
        val stillLinked = localPlaylistRepository.getSongIdsWithPlaylist(songIds).toSet()
        val songsToClear = mutableListOf<String>()
        playlist.songs.forEach { song ->
            if (song.youtubeId in stillLinked) return@forEach
            songsToClear.add(song.youtubeId)
            File(
                audioDir,
                _appContext.getString(R.string.webm_extension, song.youtubeId)
            ).takeIf { it.exists() }?.delete()
            File(
                imageDir,
                _appContext.getString(R.string.jpg_extension, song.youtubeId)
            ).takeIf { it.exists() }?.delete()
        }
        localSongRepository.deleteByIds(songsToClear)
    }

    suspend fun downloadSong(playlist: Playlist, song: Song) {
        val id = "${playlist.info.id}${song.youtubeId}"
        val existingWork = getExistingJobs(id)
        if (existingWork.isNotEmpty()) {
            printd("Download is already ongoing for song ${playlist.info.title}")
            return
        }

        localPlaylistRepository.insertPlaylistWithSongs(playlist)
        val request = OneTimeWorkRequestBuilder<SongDownloadWorker>().setInputData(
            workDataOf(
                SongDownloadWorker.PLAYLIST_KEY to playlist.info.id,
                SongDownloadWorker.SONG_KEY to song.youtubeId
            )
        ).setConstraints(
            Constraints(
                requiredNetworkType = NetworkType.CONNECTED,
                requiresStorageNotLow = true
            )
        ).build()


        workManager.enqueueUniqueWork(
            id,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun cancelAllWorks() {
        workManager.cancelAllWork()
    }

    fun getExistingJobFlow(playlist: Playlist): Flow<List<WorkInfo>> {
        return workManager.getWorkInfosForUniqueWorkFlow(playlist.info.id)

    }

    private fun getExistingJobs(id: String): List<WorkInfo> {
        return workManager.getWorkInfosForUniqueWork(id).get().filter {
            it.state == WorkInfo.State.ENQUEUED ||
                    it.state == WorkInfo.State.RUNNING ||
                    it.state == WorkInfo.State.BLOCKED
        }
    }
}