package ca.ilianokokoro.umihi.music.models

import androidx.compose.runtime.Immutable
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.room.Entity
import androidx.room.PrimaryKey
import ca.ilianokokoro.umihi.music.core.Constants
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Immutable
@Entity(tableName = Constants.Database.SONGS_TABLE)
data class Song(
    @PrimaryKey
    val youtubeId: String,
    val title: String = "",
    val artist: String = "",
    val duration: String = "",
    val thumbnailHref: String = "",
    val thumbnailPath: String? = null,
    val streamUrl: String? = null,
    val audioFilePath: String? = null,
    val uid: String = UUID.randomUUID().toString(),
) {
    val mediaItem: MediaItem
        get() =
            MediaItem.Builder()
                .setUri(youtubeId)
                .setMediaId(youtubeId)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(title)
                        .setArtist(artist)
                        .setArtworkUri(thumbnailHref.toUri())
                        .setExtras(
                            bundleOf(
                                Constants.ExoPlayer.SongMetadata.DURATION to duration,
                                Constants.ExoPlayer.SongMetadata.UID to UUID.randomUUID()
                                    .toString(),
                            )
                        )
                        .build()

                )
                .build()


    val youtubeUrl: String
        get() = "${Constants.YoutubeApi.YOUTUBE_URL_PREFIX}${youtubeId}"
    val downloaded: Boolean
        //        get() = listOf(true, false).random() // TODO
        get() = false
}


