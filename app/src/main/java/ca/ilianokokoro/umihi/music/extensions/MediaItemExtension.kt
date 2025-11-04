package ca.ilianokokoro.umihi.music.extensions

import androidx.media3.common.MediaItem
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.models.Song

fun MediaItem?.toSong(): Song {
    val extras = this?.mediaMetadata?.extras
    return Song(
        uid = extras?.getString(Constants.ExoPlayer.SongMetadata.UID).toStringOrEmpty(),
        youtubeId = this?.mediaId.toStringOrEmpty(),
        title = this?.mediaMetadata?.title.toStringOrEmpty(),
        artist = this?.mediaMetadata?.artist.toStringOrEmpty(),
        thumbnailHref = this?.mediaMetadata?.artworkUri.toString(),
        duration = extras?.getString(Constants.ExoPlayer.SongMetadata.DURATION).toStringOrEmpty()
    )
}