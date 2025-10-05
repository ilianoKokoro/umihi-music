package ca.ilianokokoro.umihi.music.models

import androidx.compose.runtime.Immutable
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import ca.ilianokokoro.umihi.music.core.Constants
import kotlinx.serialization.Serializable

@Serializable
@Immutable
@Entity(tableName = Constants.Database.PLAYLISTS_TABLE)
data class Playlist(
    @PrimaryKey
    val id: String,
    val title: String,
    val coverHref: String,
    @Ignore // TODO
    val songs: List<Song> = emptyList()
) {
    constructor(id: String, title: String, coverHref: String) : this(
        id,
        title,
        coverHref,
        listOf()
    )
    
    val mediaItems: List<MediaItem>
        get() = songs.map { song ->
            MediaItem.Builder()
                .setUri(song.id)
                .setMediaId(song.id)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setArtworkUri(song.thumbnail.toUri())
                        .build()
                )
                .build()

        }


    val downloaded: Boolean
        get() = songs.all { it.downloaded }

}