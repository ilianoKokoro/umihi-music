package ca.ilianokokoro.umihi.music.extensions

import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.Song

suspend fun Player.playPlaylist(playlist: Playlist, index: Int = 0) {
    val mediaItems = mutableListOf<MediaItem>()
    for (song in playlist.songs) {
        val mediaItem = MediaItem.Builder()
            .setUri(
                listOf(
                    "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3?_=1",
                    "https://drive.usercontent.google.com/download?id=1c6J-XHa1PilDFgOegKPjQeHhqYG9_LeA&export=download&authuser=0"
                ).random()
            )
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


    setMediaItems(mediaItems)
    seekTo(index, C.TIME_UNSET)
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
        "", currentItem?.title.toString(), currentItem?.artist.toString(),
        currentItem?.artworkUri.toString()
    )
}

