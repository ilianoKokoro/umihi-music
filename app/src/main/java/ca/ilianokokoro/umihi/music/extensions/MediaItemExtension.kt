package ca.ilianokokoro.umihi.music.extensions

import androidx.media3.common.MediaItem
import ca.ilianokokoro.umihi.music.models.Song

fun MediaItem?.toSong(): Song {
    return Song(
        this?.mediaId.toStringOrEmpty(),
        this?.mediaMetadata?.title.toStringOrEmpty(),
        this?.mediaMetadata?.artist.toStringOrEmpty(),
        this?.mediaMetadata?.artworkUri.toString()
    )
}