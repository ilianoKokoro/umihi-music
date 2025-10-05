package ca.ilianokokoro.umihi.music.data.datasources.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import ca.ilianokokoro.umihi.music.models.Playlist

@Dao
interface LocalPlaylistDataSource {
    @Insert
    suspend fun create(playlist: Playlist)

    @Insert
    suspend fun createAll(playlists: List<Playlist>)

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: String): Playlist?

    @Query("DELETE FROM playlists")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(playlist: Playlist)

}