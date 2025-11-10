package ca.ilianokokoro.umihi.music.models

import androidx.compose.runtime.Immutable
import androidx.media3.common.MediaItem
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import ca.ilianokokoro.umihi.music.core.Constants
import kotlinx.serialization.Serializable

@Serializable
@Immutable
@Entity(tableName = Constants.Database.PLAYLISTS_TABLE)
data class PlaylistInfo(
    @PrimaryKey val id: String,
    val title: String,
    val coverHref: String,
    val coverPath: String? = null,
)


@Immutable
data class Playlist(
    @Embedded val info: PlaylistInfo,
    @Relation(
        parentColumn = "id",              // Playlist.id
        entityColumn = "youtubeId",       // Song.youtubeId
        associateBy = Junction(
            value = PlaylistSongCrossRef::class,
            parentColumn = "playlistId",  // column in junction pointing to Playlist
            entityColumn = "songId"       // column in junction pointing to Song
        )
    )

    val songs: List<Song> = listOf()
) {
    val mediaItems: List<MediaItem>
        get() = songs.map { song ->
            song.mediaItem
        }

    val downloaded: Boolean
        get() = true
    //   get() = songs.all { song -> song.downloaded }

}

@Entity(
    primaryKeys = ["playlistId", "songId"],
    indices = [Index("songId")]
)
data class PlaylistSongCrossRef(
    val playlistId: String,
    val songId: String
)