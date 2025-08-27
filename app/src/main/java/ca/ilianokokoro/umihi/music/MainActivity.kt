package ca.ilianokokoro.umihi.music

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import ca.ilianokokoro.umihi.music.ui.navigation.NavigationRoot
import ca.ilianokokoro.umihi.music.ui.theme.UmihiMusicTheme
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import io.github.shalva97.initNewPipe


class MainActivity : ComponentActivity() {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private lateinit var player: Player

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNewPipe()

        initExoplayer { readyPlayer ->
            player = readyPlayer

            enableEdgeToEdge()
            setContent {
                UmihiMusicTheme {
                    NavigationRoot(
                        modifier = Modifier.fillMaxSize(),
                        player = player
                    )
                }
            }
        }
    }

    private fun initExoplayer(onReady: (Player) -> Unit) {
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture!!.addListener({
            onReady(controllerFuture!!.get())
        }, MoreExecutors.directExecutor())
    }


    override fun onDestroy() {
        super.onDestroy()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
    }
}
