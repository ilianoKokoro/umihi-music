package ca.ilianokokoro.umihi.music.models

import androidx.compose.runtime.Immutable
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import ca.ilianokokoro.umihi.music.core.Constants
import kotlinx.serialization.Serializable


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
        get() = songs.all { song -> song.downloaded }

}

@Serializable
@Immutable
@Entity(tableName = Constants.Database.PLAYLISTS_TABLE)
data class PlaylistInfo(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val coverHref: String = "",
    val coverPath: String? = null,
) {
    val isDownloadedPlaylist: Boolean
        get() = id == Constants.Downloads.DOWNLOADED_PLAYLIST_ID


    fun toBrowsableMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId("playlist:$id")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtworkUri((coverPath ?: coverHref).takeIf { it.isNotBlank() }?.toUri())
                    .setIsBrowsable(true)
                    .setIsPlayable(false)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_PLAYLIST)
                    .build()
            )
            .build()
    }
}


@Entity(
    primaryKeys = ["playlistId", "songId"],
    indices = [Index("songId")]
)
data class PlaylistSongCrossRef(
    val playlistId: String,
    val songId: String

)