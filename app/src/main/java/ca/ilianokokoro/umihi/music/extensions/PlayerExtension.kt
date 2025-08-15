package ca.ilianokokoro.umihi.music.extensions

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import ca.ilianokokoro.umihi.music.models.Song

fun Player.playSong(song: Song) {
    val mediaItem = MediaItem.Builder()
        .setUri("https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3?_=1") // TODO : actual url
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setArtworkUri(song.lowQualityCoverHref.toUri()) // TODO : High quality
                .build()
        )
        .build()

    setMediaItem(mediaItem)
    prepare()
    play()
}


fun Player.getCurrentSong(): Song {
    val currentItem = currentMediaItem?.mediaMetadata
    return Song(
        "", currentItem?.title.toString(), currentItem?.artist.toString(),
        currentItem?.artworkUri.toString()
    )
}