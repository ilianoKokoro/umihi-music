package ca.ilianokokoro.umihi.music.extensions

import androidx.media3.common.HeartRating
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaConstants
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.models.Song

val MediaMetadata.isLiked: Boolean
    get() = (userRating as? HeartRating)?.isHeart == true

fun MediaItem?.toSong(): Song {
    val extras = this?.mediaMetadata?.extras
    return Song(
        uid = extras?.getString(Constants.ExoPlayer.SongMetadata.UID).toStringOrEmpty(),
        youtubeId = this?.mediaId.toStringOrEmpty(),
        title = this?.mediaMetadata?.title.toStringOrEmpty(),
        artist = this?.mediaMetadata?.artist.toStringOrEmpty(),
        thumbnailHref = this?.mediaMetadata?.artworkUri.toString(), // TODO handle if not href
        duration = extras?.getString(Constants.ExoPlayer.SongMetadata.DURATION).toStringOrEmpty(),
        isExplicit = extras?.getLong(MediaConstants.EXTRAS_KEY_IS_EXPLICIT) == MediaConstants.EXTRAS_VALUE_ATTRIBUTE_PRESENT,
        isLiked = this?.mediaMetadata?.isLiked == true
    )
}