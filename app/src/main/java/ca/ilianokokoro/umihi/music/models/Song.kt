package ca.ilianokokoro.umihi.music.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.compose.runtime.Immutable
import androidx.core.net.toUri
import androidx.media3.common.HeartRating
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaConstants
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

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
    val uid: String = Uuid.random().toString(),
    val isExplicit: Boolean = false,
    val isLiked: Boolean? = null,
) {
    @Ignore
    var setVideoId: String? = null
    val mediaItem: MediaItem
        @UnstableApi
        get() {
            val extras = Bundle()
            extras.putString(Constants.ExoPlayer.SongMetadata.DURATION, duration)
            extras.putString(Constants.ExoPlayer.SongMetadata.UID, Uuid.random().toString())
            if (isExplicit) {
                extras.putLong(
                    MediaConstants.EXTRAS_KEY_IS_EXPLICIT,
                    MediaConstants.EXTRAS_VALUE_ATTRIBUTE_PRESENT
                )
            }
            val rating = isLiked?.let { HeartRating(it) } ?: HeartRating()

            extras.putLong(
                MediaConstants.EXTRAS_KEY_DOWNLOAD_STATUS,
                if (downloaded) {
                    MediaConstants.EXTRAS_VALUE_STATUS_DOWNLOADED
                } else {
                    MediaConstants.EXTRAS_VALUE_STATUS_NOT_DOWNLOADED
                }
            )

            return MediaItem.Builder()
                .setUri(youtubeUrl)
                .setMediaId(youtubeId)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(title)
                        .setArtist(artist)
                        .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                        .setIsBrowsable(false)
                        .setIsPlayable(true)
                        .setArtworkUri((thumbnailPath ?: thumbnailHref).toUri())
                        .setUserRating(rating)
                        .setExtras(extras)
                        .build()

                )
                .build()
        }


    val youtubeUrl: String
        get() = "${Constants.YoutubeApi.YOUTUBE_URL_PREFIX}${youtubeId}"
    val downloaded: Boolean
        get() = audioFilePath != null && thumbnailPath != null


    suspend fun getThumbnailBitmap(): Bitmap? {
        val bytes = UmihiHelper.fetchArtworkBytes(thumbnailHref)
        return bytes?.let {
            BitmapFactory.decodeByteArray(it, 0, it.size)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Song) {
            return false
        }
        return this.hashCode() == other.hashCode()
    }

    override fun hashCode(): Int {
        return youtubeId.hashCode() + 31 * uid.hashCode()
    }

    fun isSameYoutubeSong(other: Song): Boolean {
        return this.youtubeId == other.youtubeId
    }

    companion object {
        fun createFromYoutubeUrl(url: String): Song {
            return Song(youtubeId = url.removePrefix(Constants.YoutubeApi.YOUTUBE_URL_PREFIX))
        }

    }

}


