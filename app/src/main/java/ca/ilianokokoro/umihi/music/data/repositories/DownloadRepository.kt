package ca.ilianokokoro.umihi.music.data.repositories

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import ca.ilianokokoro.umihi.music.core.helpers.ConnectivityHelper
import ca.ilianokokoro.umihi.music.core.helpers.FileHelper
import ca.ilianokokoro.umihi.music.core.helpers.LogHelper.printd
import ca.ilianokokoro.umihi.music.core.managers.NotificationManager
import ca.ilianokokoro.umihi.music.core.workers.PlaylistDownloadWorker
import ca.ilianokokoro.umihi.music.core.workers.SongDownloadWorker
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DownloadRepository(appContext: Context) {
    private val _appContext = appContext
    private val workManager: WorkManager = WorkManager.getInstance(_appContext)
    private val localPlaylistRepository = AppDatabase.getInstance(_appContext).playlistRepository()
    private val localSongRepository = AppDatabase.getInstance(_appContext).songRepository()

    suspend fun downloadPlaylist(playlist: Playlist, useMetered: Boolean = false) {
        val existingWork = getExistingJobs(playlist.info.id)
        if (existingWork.isNotEmpty()) {
            printd("Download is already ongoing for playlist ${playlist.info.title}")
            return
        }
        localPlaylistRepository.insertPlaylistWithSongs(
            playlist.copy(
                info = playlist.info.copy(shouldBeDownloaded = true)
            )
        )
        val request = OneTimeWorkRequestBuilder<PlaylistDownloadWorker>().setInputData(
            workDataOf(
                PlaylistDownloadWorker.PLAYLIST_KEY to playlist.info.id
            )
        ).setConstraints(
            Constraints(
                requiredNetworkType = if (useMetered) NetworkType.CONNECTED else NetworkType.UNMETERED,
                requiresStorageNotLow = true
            )
        ).build()


        workManager.enqueueUniqueWork(playlist.info.id, ExistingWorkPolicy.KEEP, request)

        if (!useMetered && ConnectivityHelper.isMeteredNetwork(_appContext)) {
            NotificationManager.showPlaylistDownloadWaitingForWifi(_appContext, playlist)
        }
    }

    suspend fun deletePlaylist(context: Context, playlist: Playlist) = withContext(Dispatchers.IO) {
        val localPlaylist = localPlaylistRepository.getPlaylistById(playlist.info.id) ?: playlist

        FileHelper.deleteStoredFile(context, localPlaylist.info.coverPath)
        localPlaylistRepository.deleteFullPlaylist(localPlaylist.info.id)

        val stillLinked = localPlaylistRepository
            .getSongIdsWithPlaylist(localPlaylist.songs.map { it.youtubeId })
            .toSet()

        val orphaned = localPlaylist.songs.filter { it.youtubeId !in stillLinked }

        orphaned.forEach { song ->
            FileHelper.deleteStoredFile(context, song.audioFilePath)
            FileHelper.deleteStoredFile(context, song.thumbnailPath)
        }

        if (orphaned.isNotEmpty()) {
            localSongRepository.deleteByIds(orphaned.map { it.youtubeId })
        }
    }

    suspend fun downloadSong(playlist: Playlist, song: Song, useMetered: Boolean = false) {
        val id = "${playlist.info.id}${song.youtubeId}"
        val existingWork = getExistingJobs(id)
        if (existingWork.isNotEmpty()) {
            printd("Download is already ongoing for song ${playlist.info.title}")
            return
        }

        localPlaylistRepository.insertPlaylistWithSongs(playlist)
        enqueueSongDownload(song, id, playlist.info.id, useMetered)
    }

    suspend fun downloadSong(song: Song, useMetered: Boolean = false) {
        val existingSong = localSongRepository.getSong(song.youtubeId)
        if (existingSong?.downloaded == true) {
            printd("Song is already downloaded ${song.youtubeId}")
            return
        }

        val existingWork = getExistingJobs(song.youtubeId)
        if (existingWork.isNotEmpty()) {
            printd("Download is already ongoing for song ${song.youtubeId}")
            return
        }

        if (existingSong == null) {
            localSongRepository.create(song)
        }
        enqueueSongDownload(song, song.youtubeId, null, useMetered)
    }

    private suspend fun enqueueSongDownload(
        song: Song,
        uniqueWorkId: String,
        playlistId: String?,
        useMetered: Boolean
    ) {
        val builder = OneTimeWorkRequestBuilder<SongDownloadWorker>().setConstraints(
            Constraints(
                requiredNetworkType = if (useMetered) NetworkType.CONNECTED else NetworkType.UNMETERED,
                requiresStorageNotLow = true
            )
        )

        val data = if (playlistId != null) {
            workDataOf(
                SongDownloadWorker.PLAYLIST_KEY to playlistId,
                SongDownloadWorker.SONG_KEY to song.youtubeId
            )
        } else {
            workDataOf(SongDownloadWorker.SONG_KEY to song.youtubeId)
        }
        builder.setInputData(data)

        workManager.enqueueUniqueWork(
            uniqueWorkId,
            ExistingWorkPolicy.KEEP,
            builder.build()
        )

        if (!useMetered && ConnectivityHelper.isMeteredNetwork(_appContext)) {
            NotificationManager.showSongDownloadWaitingForWifi(_appContext, song)
        }
    }

    suspend fun cancelPlaylistDownload(playlist: Playlist) {
        printd("stopping work ${playlist.info.title}")
        workManager.cancelUniqueWork(playlist.info.id)
        
        localPlaylistRepository.getPlaylistById(playlist.info.id)?.let { stored ->
            localPlaylistRepository.insertPlaylist(
                stored.info.copy(shouldBeDownloaded = false)
            )
        }
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