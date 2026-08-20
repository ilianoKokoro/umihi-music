package ca.ilianokokoro.umihi.music.data.repositories

import android.content.Context
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.models.HistoryEntry
import ca.ilianokokoro.umihi.music.models.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class HistoryRepository(context: Context) {
    private val historyDao = AppDatabase.getInstance(context).historyDao()

    fun getRecentHistorySongs(limit: Int = 30): Flow<List<Song>> {
        return historyDao.getRecentHistoryFlow(limit)
            .map { list -> list.map { it.toSong() } }
            .flowOn(Dispatchers.IO)
    }

    suspend fun getRecentSongsList(limit: Int = 30): List<Song> = withContext(Dispatchers.IO) {
        historyDao.getRecentHistory(limit).map { it.toSong() }
    }

    suspend fun addSongToHistory(song: Song) = withContext(Dispatchers.IO) {
        if (song.youtubeId.isBlank()) return@withContext
        val entry = HistoryEntry(
            youtubeId = song.youtubeId,
            title = song.title,
            artist = song.artist,
            duration = song.duration,
            thumbnailHref = song.thumbnailPath ?: song.thumbnailHref,
            isVideo = song.isVideo,
            playedAt = System.currentTimeMillis()
        )
        historyDao.insertOrUpdate(entry)
    }

    suspend fun removeSongFromHistory(youtubeId: String) = withContext(Dispatchers.IO) {
        historyDao.delete(youtubeId)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        historyDao.clearAll()
    }
}
