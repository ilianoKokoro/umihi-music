package ca.ilianokokoro.umihi.music.core

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

object Constants {
    const val BETA_SUFFIX = "-beta"

    object Url {
        const val DISCORD_INVITE = "https://discord.gg/mSPeHS5cF6"

        object Github {
            object Beta {
                const val API =
                    "https://api.github.com/repos/ilianoKokoro/umihi-music/commits/main"
                const val DOWNLOAD =
                    "https://github.com/ilianoKokoro/umihi-music/releases/download/beta/UmihiMusic.apk"
            }

            object Release {
                const val API =
                    "https://api.github.com/repos/ilianoKokoro/umihi-music/releases/latest"
                const val DOWNLOAD =
                    "https://github.com/ilianoKokoro/umihi-music/releases/latest/download/UmihiMusic.apk"

            }
        }
    }

    object Downloads {
        const val MAX_CONCURRENT_DOWNLOADS = 8
        const val DIRECTORY = "downloads"
        const val THUMBNAILS_FOLDER = "thumbnails_downloads"
        const val AUDIO_FILES_FOLDER = "audio_files_downloads"
        const val DOWNLOADED_PLAYLIST_ID = "_downloaded_"

        const val UPDATE_APK = "update.apk"
    }

    object Locale {
        object Special {
            const val CODE = "eo"
            const val CLICK_QUANTITY = 25

        }
    }

    object Marquee {
        const val DELAY = 2000
    }

    object Ui {
        object MiniPlayer {
            val HEIGHT = 70.dp
        }

        val SCROLLABLE_BOTTOM_PADDING = 100.dp
        const val WEAROS_MAX_IMAGE_SIZE = 720
    }

    object Animation {
        const val NAVIGATION_DURATION = 200
        const val IMAGE_FADE_DURATION = 200

        val SLIDE_UP_TRANSITION = NavDisplay.transitionSpec {
            slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(
                    durationMillis = NAVIGATION_DURATION * 2,
                    easing = FastOutSlowInEasing
                )
            ) togetherWith fadeOut()
        } + NavDisplay.popTransitionSpec {
            fadeIn() togetherWith slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(
                    durationMillis = NAVIGATION_DURATION * 2,
                    easing = FastOutSlowInEasing
                )
            )
        } + NavDisplay.predictivePopTransitionSpec {
            fadeIn() togetherWith slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(
                    durationMillis = NAVIGATION_DURATION * 2,
                    easing = FastOutSlowInEasing
                )
            )
        }

    }

    object Auth {
        const val START_URL =
            "https://accounts.google.com/ServiceLogin?ltmpl=music&service=youtube&uilel=3&passive=true&continue=https%3A%2F%2Fwww.youtube.com%2Fsignin%3Faction_handle_signin%3Dtrue%26app%3Ddesktop%26hl%3Den%26next%3Dhttps%253A%252F%252Fmusic.youtube.com%252F%26feature%3D__FEATURE__&hl=en"
        const val END_URL =
            "https://music.youtube.com/"
    }

    object Datastore {
        const val NAME = "umihi-mobile"
        const val COOKIES_KEY = "cookies"
        const val UPDATE_CHANNEL_KEY = "update-channel"
        const val DATA_SYNC_ID = "data-sync-id"
        const val SHOW_PODCAST_PLAYLIST = "show-podcast-playlist"
        const val USE_SPECIAL_LANGUAGE = "use-special-language"
        const val USE_AUDIO_OFFLOAD = "use-audio-offload"
        const val KEEP_SCREEN_ON = "keep-screen-on"

    }

    object Database {
        const val NAME = "umihi-music"
        const val VERSION = 6
        const val SONGS_TABLE = "songs"
        const val PLAYLISTS_TABLE = "playlists"
        const val VERSIONS_TABLE = "versions"
    }

    object ExoPlayer {
        object Cache {
            const val NAME = "umihi-music-exoplayer"
            const val SIZE: Long = 1000L * 1024L * 1024L // 1000 MB

            object Library {
                const val ROOT_ID = "root"
                const val PLAYLIST_ROOT = "root_playlist"
                const val PLAYLIST_PREFIX = "playlist:"
            }
        }

        object SongMetadata {
            const val DURATION = "duration"
            const val UID = "uid"
        }
    }

    object Player {
        const val PROGRESS_UPDATE_DELAY = 500L
        const val IMAGE_TRANSITION_DELAY = 200
    }

    object YoutubeApi {
        const val URL_REGEX =
            """https?://(www\.)?(youtube\.com|youtu\.be|music\.youtube\.com)/\S+"""
        const val RETRY_COUNT = 3
        const val PODCAST_PLAYLIST_ID = "VLSE"
        const val RETRY_DELAY = 1000L
        const val YOUTUBE_URL_PREFIX = "https://www.youtube.com/watch?v="
        const val ORIGIN = "https://music.youtube.com"
        const val API_KEY = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30"
        const val USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36"

        object Browse {
            const val URL = "${ORIGIN}/youtubei/v1/browse?key=${API_KEY}"
            const val PLAYLIST_BROWSE_ID = "FEmusic_liked_playlists"

        }

        object Client {
            val WEB_REMIX =
                buildJsonObject {
                    put("clientName", JsonPrimitive("WEB_REMIX"))
                    put("clientVersion", JsonPrimitive("1.20250212.01.00"))
                    put("userAgent", JsonPrimitive(USER_AGENT))
                    put("xClientName", JsonPrimitive("67"))
                }

            val ANDROID_VR = buildJsonObject {
                put("clientName", JsonPrimitive("ANDROID_VR"))
                put("clientVersion", JsonPrimitive("1.61.48"))
                put("clientId", JsonPrimitive("28"))

                put(
                    "userAgent",
                    JsonPrimitive(
                        "com.google.android.apps.youtube.vr.oculus/1.61.48 (Linux; U; Android 12; en_US; Quest 3; Build/SQ3A.220605.009.A1; Cronet/132.0.6808.3)"
                    )
                )

                put("osName", JsonPrimitive("Android"))
                put("osVersion", JsonPrimitive("12"))
                put("deviceMake", JsonPrimitive("Oculus"))
                put("deviceModel", JsonPrimitive("Quest 3"))
                put("androidSdkVersion", JsonPrimitive("32"))
                put("buildId", JsonPrimitive("SQ3A.220605.009.A1"))
                put("cronetVersion", JsonPrimitive("132.0.6808.3"))
                put("packageName", JsonPrimitive("com.google.android.apps.youtube.vr.oculus"))
            }
        }

        object Create {
            const val URL = "${ORIGIN}/youtubei/v1/playlist/create?key=${API_KEY}"
        }


        object Delete {
            const val URL = "${ORIGIN}/youtubei/v1/playlist/delete?key=${API_KEY}"
        }

        object PlayerInfo {
            const val URL =
                "https://www.youtube.com/youtubei/v1/player"

        }

        object Search {
            const val URL = "https://music.youtube.com/youtubei/v1/search"
            const val FILTER = "EgWKAQIIAWoSEAMQBBAQEAUQFRAKEAkQERAO"
        }


    }
}