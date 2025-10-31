package ca.ilianokokoro.umihi.music.extensions

import androidx.media3.common.MediaItem
import ca.ilianokokoro.umihi.music.models.Song

fun MediaItem?.toSong(): Song {
    return Song(
        youtubeId = this?.mediaId.toStringOrEmpty(),
        title = this?.mediaMetadata?.title.toStringOrEmpty(),
        artist = this?.mediaMetadata?.artist.toStringOrEmpty(),
        thumbnailHref = this?.mediaMetadata?.artworkUri.toString()
    )
}