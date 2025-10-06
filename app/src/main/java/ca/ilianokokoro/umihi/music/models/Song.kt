package ca.ilianokokoro.umihi.music.models

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import ca.ilianokokoro.umihi.music.core.Constants
import kotlinx.serialization.Serializable

@Serializable
@Immutable
@Entity(tableName = Constants.Database.SONGS_TABLE)
data class Song(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val thumbnailHref: String,
    val thumbnailPath: String? = null,
    val streamUrl: String? = null,
    val audioFilePath: String? = null
) {
    val youtubeUrl: String
        get() = "${Constants.YoutubeApi.YOUTUBE_URL_PREFIX}${id}"
    val downloaded: Boolean
        get() = listOf(true, false).random() // TODO
}


