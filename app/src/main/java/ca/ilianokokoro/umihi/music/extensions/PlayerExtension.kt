package ca.ilianokokoro.umihi.music.extensions

import androidx.media3.common.C
import androidx.media3.common.Player
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.Song

suspend fun Player.playPlaylist(playlist: Playlist, index: Int = 0) {
    // Put placeholder data
    var mediaItems = playlist.getMediaItems()
    setMediaItems(mediaItems, index, C.TIME_UNSET)
    prepare()

    // Put placeholder data
    mediaItems = playlist.getMediaItems(true)
    setMediaItems(mediaItems, index, C.TIME_UNSET)
    prepare()
    play()
}

suspend fun Player.shufflePlaylist(playlist: Playlist) {
    val songs = playlist.songs
    val shuffledPlaylist = playlist.copy(songs = songs.shuffled())
    this.playPlaylist(shuffledPlaylist)
}

fun Player.getCurrentSong(): Song {
    val currentItem = currentMediaItem?.mediaMetadata
    return Song(
        currentMediaItem?.mediaId ?: "",
        currentItem?.title.toStringOrEmpty(),
        currentItem?.artist.toStringOrEmpty(),
        currentItem?.artworkUri.toString()
    )
}
