package ca.ilianokokoro.umihi.music.data.datasources

import android.util.Log
import ca.ilianokokoro.umihi.music.core.helpers.YoutubeHelper
import ca.ilianokokoro.umihi.music.core.helpers.YoutubeRequestHelper
import ca.ilianokokoro.umihi.music.models.Cookies
import ca.ilianokokoro.umihi.music.models.Song

class SongDataSource {
    fun getStreamUrlFromId(song: Song, cookies: Cookies): String {
// TODO : REmove cookies ?
        val result = YoutubeRequestHelper.getPlayerInfo(song.id)
        Log.d("CustomLog", result)

        // Hard stream
        // return "https://rr1---sn-tt1e7nls.googlevideo.com/videoplayback?expire=1755218997&ei=1S-eaIXnD5qP2_gP8ZWB4QM&ip=23.239.50.100&id=o-AIEIXiVyo4lL5UgIc8U-LC2XDeqAR6bmuQXZEdmAKgVe&itag=251&source=youtube&requiressl=yes&xpc=EgVo2aDSNQ%3D%3D&cps=214&met=1755197397%2C&mh=Fk&mm=31%2C26&mn=sn-tt1e7nls%2Csn-vgqskn6s&ms=au%2Conr&mv=m&mvi=1&pl=19&rms=au%2Cau&gcr=ca&initcwndbps=2373750&bui=AY1jyLODGdIolOHMyIdNSAB74Q10c4PKh3tehHxMmBV1r0uau7dwyJpoUhV7Hptk7_IriLoeijiatRwO&spc=l3OVKYfd6eLn2xv8wmvi3wMzcNEYNg7aSxal5vQZcntPzthKW6UmPXj-&vprv=1&svpuc=1&mime=audio%2Fwebm&rqh=1&gir=yes&clen=3309418&dur=200.841&lmt=1742587434527249&mt=1755197100&fvip=1&keepalive=yes&fexp=51548755%2C51552688&c=ANDROID_VR&txp=2318224&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cxpc%2Cgcr%2Cbui%2Cspc%2Cvprv%2Csvpuc%2Cmime%2Crqh%2Cgir%2Cclen%2Cdur%2Clmt&sig=AJfQdSswRQIgMTTUXG4z3iCFZWPiYvFJ0mk-rmU3lSenLh6Ds9ukCZ0CIQDASw1PkgdHJkWcN7pXVuj-H-_XMZAJjRvHrZLx0oikPQ%3D%3D&lsparams=cps%2Cmet%2Cmh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Crms%2Cini"

        // Easy stream
        return listOf(
            "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3?_=1"
        ).random()
    }

    fun getFullSongInfo(song: Song): Song {
        return song.copy(
            highQualityCoverHref = YoutubeHelper.extractHighQualityThumbnail(
                YoutubeRequestHelper.getPlayerInfo(song.id)
            )
        )
    }
}