package ca.ilianokokoro.umihi.music.data.datasources.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ca.ilianokokoro.umihi.music.models.HistoryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: HistoryEntry)

    @Query("SELECT * FROM playback_history ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentHistoryFlow(limit: Int = 30): Flow<List<HistoryEntry>>

    @Query("SELECT * FROM playback_history ORDER BY playedAt DESC LIMIT :limit")
    suspend fun getRecentHistory(limit: Int = 30): List<HistoryEntry>

    @Query("DELETE FROM playback_history WHERE youtubeId = :youtubeId")
    suspend fun delete(youtubeId: String)

    @Query("DELETE FROM playback_history")
    suspend fun clearAll()
}
