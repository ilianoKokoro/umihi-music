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
            .setUri(
                "https://rr5---sn-tt1e7nlz.googlevideo.com/videoplayback?expire=1755478044&ei=vCOiaOORAsezlu8P5cCHsAI&ip=23.239.50.100&id=o-AJ-OCvPVqdxkHzptI0G5PL_qz2j2xMp2w_nwYbIUfcRr&itag=251&source=youtube&requiressl=yes&xpc=EgVo2aDSNQ%3D%3D&cps=0&met=1755456444%2C&mh=19&mm=31%2C26&mn=sn-tt1e7nlz%2Csn-t0aedn7l&ms=au%2Conr&mv=m&mvi=5&pl=19&rms=au%2Cau&pcm2=yes&initcwndbps=2281250&bui=AY1jyLODr5QZtpCCiusSJV7MUf04dwJu_AbQfdFYEcGzJdFu7VjmPgQq6z7bnVeyuFHfAD4m0oDEPglP&spc=l3OVKWgfFKF3CzLO_A5PTn832cvbSwJMRfNm7CeZu2OWaGRmIUFFhUgslvhNIQ&vprv=1&svpuc=1&mime=audio%2Fwebm&rqh=1&gir=yes&clen=3628073&dur=219.221&lmt=1754973393647450&mt=1755456075&fvip=4&keepalive=yes&fexp=51548755%2C51565115%2C51565681&c=IOS&txp=6208224&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cxpc%2Cpcm2%2Cbui%2Cspc%2Cvprv%2Csvpuc%2Cmime%2Crqh%2Cgir%2Cclen%2Cdur%2Clmt&sig=AJfQdSswRAIgdWggUAAwSP0jNL8316VjExh-1RVpCWXcFjQU3wS_9hkCIDIHVjNDPhzWF2781xpn_tHwiJj9dsoNDaPdyEEow0t3&lsparams=cps%2Cmet%2Cmh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Crms%2C"
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

