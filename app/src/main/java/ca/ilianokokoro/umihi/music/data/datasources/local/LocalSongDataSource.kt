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
    suspend fun getSong(songId: String): Song?

    @Query("DELETE FROM songs")
    suspend fun deleteAll()


    suspend fun setStreamUrl(songId: String, streamUrl: String) {
        val existing = getSong(songId) ?: Song(
            youtubeId = songId,
            streamUrl = streamUrl
        )

        val updated = existing.copy(streamUrl = streamUrl)
        create(updated)
    }

    @Delete
    suspend fun delete(song: Song)

}