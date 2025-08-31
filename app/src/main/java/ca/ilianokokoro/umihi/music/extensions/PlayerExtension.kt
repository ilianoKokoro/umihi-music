package ca.ilianokokoro.umihi.music.extensions

import androidx.media3.common.C
import androidx.media3.common.Player
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.Song

fun Player.playPlaylist(playlist: Playlist, index: Int = 0) {
    setMediaItems(playlist.getMediaItems(), index, C.TIME_UNSET)
    prepare()
    play()
}

fun Player.shufflePlaylist(playlist: Playlist) {
    val songs = playlist.songs
    val shuffledPlaylist = playlist.copy(songs = songs.shuffled())
    this.playPlaylist(shuffledPlaylist)
}


fun Player.getQueue(): List<Song> {
    val queue = mutableListOf<Song>()
    for (i in 0 until mediaItemCount) {
        val mediaItem = getMediaItemAt(i)
        queue.add(mediaItem.toSong())
    }
    return queue
}

fun Player.getCurrentSong(): Song {
    return currentMediaItem.toSong()
}