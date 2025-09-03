package ca.ilianokokoro.umihi.music.core

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

object Constants {
    object Marquee {
        const val DELAY = 2000
    }

    object Animation {
        const val NAVIGATION_DURATION = 200
        const val IMAGE_FADE_DURATION = 200
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

    object Database {
        const val NAME = "umihi-music"
        const val VERSION = 1
        const val SONGS_TABLE = "songs"
    }

    object Player {
        const val PROGRESS_UPDATE_DELAY = 500L
        const val IMAGE_TRANSITION_DELAY = 200
    }

    object YoutubeApi {
        const val YOUTUBE_URL_PREFIX = "https://www.youtube.com/watch?v="
        const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36"

        const val ORIGIN = "https://music.youtube.com"
        const val API_KEY = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30"

        object Browse {
            const val URL = "${ORIGIN}/youtubei/v1/browse?key=${API_KEY}"
            const val PLAYLIST_BROWSE_ID = "FEmusic_liked_playlists"
            val CONTEXT =
                buildJsonObject {
                    put("client", buildJsonObject {
                        put("clientName", JsonPrimitive("WEB_REMIX"))
                        put("clientVersion", JsonPrimitive("1.20250212.01.00"))
                    })
                }

        }

        object PlayerInfo {

            const val URL =
                "https://www.youtube.com/youtubei/v1/player"

        }


    }
}