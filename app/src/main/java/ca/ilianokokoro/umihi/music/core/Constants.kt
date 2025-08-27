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

    object Player {
        const val PROGRESS_UPDATE_DELAY = 500L
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

            val CONTEXT = buildJsonObject {
                put("client", buildJsonObject {
                    put("clientName", JsonPrimitive("WEB"))
                    put("clientVersion", JsonPrimitive("2.20250312.04.00"))
                    put(
                        "userAgent",
                        JsonPrimitive(
                            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.5 Safari/605.1.15,gzip(gfe)"
                        )
                    )
                    put("hl", JsonPrimitive("en"))
                    put("timeZone", JsonPrimitive("UTC"))
                    put("utcOffsetMinutes", JsonPrimitive(0))
                })
            }


        }


    }
}