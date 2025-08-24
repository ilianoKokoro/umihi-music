package ca.ilianokokoro.umihi.music.extensions

import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.Song

fun Player.playPlaylist(playlist: Playlist, index: Int = 0) {
    val mediaItems = mutableListOf<MediaItem>()
    for (song in playlist.songs) {
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setArtworkUri(
                        song.thumbnail.toUri()
                    )
                    .build()
            )
            .build()
        mediaItems.add(mediaItem)
    }

    setMediaItems(mediaItems, index, C.TIME_UNSET)
    prepare()
    play()
}


fun Player.shufflePlaylist(playlist: Playlist) {
    val songs = playlist.songs
    val shuffledPlaylist = playlist.copy(songs = songs.shuffled())
    this.playPlaylist(shuffledPlaylist)
}


fun Player.getCurrentSong(): Song {
    val currentItem = currentMediaItem?.mediaMetadata
    return Song(
        currentMediaItem?.mediaId ?: "",
        currentItem?.title.toString(),
        currentItem?.artist.toString(),
        currentItem?.artworkUri.toString()
    )
}

