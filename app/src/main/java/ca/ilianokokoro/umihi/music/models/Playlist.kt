package ca.ilianokokoro.umihi.music.models

import androidx.compose.runtime.Immutable
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import ca.ilianokokoro.umihi.music.core.helpers.YoutubeHelper
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class Playlist(
    val id: String,
    val title: String,
    val coverHref: String,
    val songs: List<Song> = emptyList()
) {
    suspend fun getMediaItems(getUri: Boolean = false): List<MediaItem> {
        return songs.map { song ->
            val uri = if (getUri) {
                YoutubeHelper.getSongPlayerUrl(song.id)
            } else {
                ""
            }

            MediaItem.Builder()
                .setUri(uri)
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
    }

}