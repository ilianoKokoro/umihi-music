package ca.ilianokokoro.umihi.music.core

object Constants {
    object Marquee {
        const val DELAY = 2000
    }

    object Transition {
        const val DURATION = 200

    }

    object Auth {
        const val START_URL =
            "https://accounts.google.com/AccountChooser?service=youtube&continue=https://music.youtube.com/feed/library"
        const val END_URL =
            "https://music.youtube.com/"
    }

    object Datastore {
        const val NAME = "umihi-mobile"
        const val COOKIES_KEY = "cookies"
    }

    object YoutubeApi {
        const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36"

        const val ORIGIN = "https://music.youtube.com"
        const val KEY = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30"

        object Browse {
            const val URL = "${ORIGIN}/youtubei/v1/browse"
            const val PLAYLIST_ID = "FEmusic_liked_playlists"
        }

        val CONTEXT = mapOf(
            "client" to mapOf("clientName" to "WEB_REMIX", "clientVersion" to "1.20250212.01.00")
        )
    }
}