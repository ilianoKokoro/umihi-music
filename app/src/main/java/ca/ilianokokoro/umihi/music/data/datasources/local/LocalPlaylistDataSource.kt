package ca.ilianokokoro.umihi.music.data.datasources.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.PlaylistSongCrossRef
import ca.ilianokokoro.umihi.music.models.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalPlaylistDataSource {
    @Transaction
    @Query("SELECT * FROM playlists")
    suspend fun getAll(): List<Playlist>

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: String): Playlist?

    @Query(
        """
    SELECT DISTINCT songId
    FROM PlaylistSongCrossRef
    WHERE songId IN (:songIds)
"""
    )
    suspend fun getSongIdsWithPlaylist(songIds: List<String>): List<String>

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun observePlaylistById(playlistId: String): Flow<Playlist?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlistInfo: PlaylistInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(refs: List<PlaylistSongCrossRef>)

    @Transaction
    suspend fun insertPlaylistWithSongs(
        playlist: Playlist,
    ) {
        insertPlaylist(playlist.info)
        val songs = playlist.songs
        insertSongs(songs)
        val refs = songs.map { song -> PlaylistSongCrossRef(playlist.info.id, song.youtubeId) }
        insertCrossRefs(refs)
    }

    @Query("DELETE FROM playlists")
    suspend fun deleteAll()

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: String)

    @Query("DELETE FROM PlaylistSongCrossRef WHERE playlistId = :playlistId")
    suspend fun deleteCrossRefsByPlaylistId(playlistId: String)

    @Transaction
    suspend fun deleteFullPlaylist(playlistId: String) {
        deleteCrossRefsByPlaylistId(playlistId)
        deletePlaylistById(playlistId)
    }
}