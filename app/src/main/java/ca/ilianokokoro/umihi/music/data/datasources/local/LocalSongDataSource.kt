package ca.ilianokokoro.umihi.music.data.datasources.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ca.ilianokokoro.umihi.music.models.Song

@Dao
interface LocalSongDataSource {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun create(song: Song)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createAll(songs: List<Song>)

    @Query("SELECT * FROM songs WHERE youtubeId = :songId")
    suspend fun getSongById(songId: String): Song?

    @Query("DELETE FROM songs")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(song: Song)

}