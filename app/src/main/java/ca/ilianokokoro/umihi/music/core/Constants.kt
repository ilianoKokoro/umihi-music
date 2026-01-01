package ca.ilianokokoro.umihi.music.core

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

object Constants {
    const val BETA_SUFFIX = "-beta"

    object Url {
        const val DISCORD_INVITE = "https://discord.gg/mSPeHS5cF6"
        const val GITHUB_RELEASE_API =
            "https://api.github.com/repos/ilianoKokoro/umihi-music/releases/latest"
        const val GITHUB_COMMIT_API =
            "https://api.github.com/repos/ilianoKokoro/umihi-music/commits/main"


        const val GITHUB_RELEASE_LINK =
            "https://github.com/ilianoKokoro/umihi-music/releases/latest"
    }

    object Downloads {
        const val MAX_CONCURRENT_DOWNLOADS = 5
        const val DIRECTORY = "downloads"
        const val THUMBNAILS_FOLDER = "thumbnails_downloads"
        const val AUDIO_FILES_FOLDER = "audio_files_downloads"
    }

    object Marquee {
        const val DELAY = 2000
    }

    object Animation {
        const val NAVIGATION_DURATION = 200
        const val IMAGE_FADE_DURATION = 200

        val SLIDE_UP_TRANSITION = NavDisplay.transitionSpec {
            slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(NAVIGATION_DURATION * 2)
            ) togetherWith ExitTransition.KeepUntilTransitionsFinished
        } + NavDisplay.popTransitionSpec {
            EnterTransition.None togetherWith
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(NAVIGATION_DURATION * 2)
                    )
        } + NavDisplay.predictivePopTransitionSpec {
            EnterTransition.None togetherWith
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(NAVIGATION_DURATION * 2)
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
        const val RETRY_COUNT = 3

        const val RETRY_DELAY = 1000L
        const val YOUTUBE_URL_PREFIX = "https://www.youtube.com/watch?v="
        const val ORIGIN = "https://music.youtube.com"
        const val API_KEY = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30"
        const val USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36"

        object Browse {
            const val URL = "${ORIGIN}/youtubei/v1/browse?key=${API_KEY}"
            const val PLAYLIST_BROWSE_ID = "FEmusic_liked_playlists"
            val CLIENT =
                buildJsonObject {
                    put("client", buildJsonObject {
                        put("clientName", JsonPrimitive("WEB_REMIX"))
                        put("clientVersion", JsonPrimitive("1.20250212.01.00"))
                        put(
                            "userAgent",
                            JsonPrimitive(USER_AGENT)
                        )
                        put("xClientName", JsonPrimitive(1))
                    })
                }

        }

        object PlayerInfo {

            const val URL =
                "https://www.youtube.com/youtubei/v1/player"


        }


    }
}