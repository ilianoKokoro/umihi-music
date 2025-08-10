package ca.ilianokokoro.umihi.music

import android.app.Application
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession

class PlayerApp : Application() {
    lateinit var player: ExoPlayer
        private set
    lateinit var mediaSession: MediaSession
        private set

    override fun onCreate() {
        super.onCreate()

        // Create the player once for the whole app
        player = ExoPlayer.Builder(this).build()

        // MediaSession is required for background + notification controls
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onTerminate() {
        mediaSession.release()
        player.release()
        super.onTerminate()
    }
}