package ca.ilianokokoro.umihi.music.models

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Immutable
@Entity(tableName = "playback_history")
data class HistoryEntry(
    @PrimaryKey
    val youtubeId: String,
    val title: String = "",
    val artist: String = "",
    val duration: String = "",
    val thumbnailHref: String = "",
    val isVideo: Boolean = false,
    val playedAt: Long = System.currentTimeMillis()
) {
    fun toSong(): Song {
        return Song(
            youtubeId = youtubeId,
            title = title,
            artist = artist,
            duration = duration,
            thumbnailHref = thumbnailHref
        ).also {
            it.isVideo = isVideo
        }
    }
}
