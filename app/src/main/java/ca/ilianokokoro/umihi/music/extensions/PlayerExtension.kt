package ca.ilianokokoro.umihi.music.extensions

import android.content.Context
import android.widget.Toast
import androidx.media3.common.C
import androidx.media3.common.Player
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.Song

fun Player.playPlaylist(playlist: Playlist, index: Int = 0) {
    setMediaItems(playlist.mediaItems, index, C.TIME_UNSET)
    prepare()
    play()
}

fun Player.shufflePlaylist(playlist: Playlist) {
    val songs = playlist.songs
    val shuffledPlaylist = playlist.copy(songs = songs.shuffled())
    playPlaylist(shuffledPlaylist)
}


fun Player.getQueue(): List<Song> {
    val queue = mutableListOf<Song>()
    for (i in 0 until mediaItemCount) {
        val mediaItem = getMediaItemAt(i)
        queue.add(mediaItem.toSong())
    }
    return queue
}

fun Player.addNext(song: Song, context: Context? = null) {
    addMediaItem(currentMediaItemIndex + 1, song.mediaItem)
    if (context != null) {
        Toast.makeText(context, context.getString(R.string.play_next_toast), Toast.LENGTH_SHORT)
            .show()
    }
    playIfQueueCreated()
}


fun Player.addToQueue(song: Song, context: Context? = null) {
    addMediaItem(getQueue().size, song.mediaItem)
    if (context != null) {
        Toast.makeText(context, context.getString(R.string.added_queue_toast), Toast.LENGTH_SHORT)
            .show()
    }
    playIfQueueCreated()
}

fun Player.clearQueue() {
    stop()
    clearMediaItems()
}

fun Player.getCurrentSong(): Song {
    return currentMediaItem.toSong()
}

private fun Player.playIfQueueCreated() {
    if (this.getQueue().size == 1) {
        prepare()
        play()
    }
}

