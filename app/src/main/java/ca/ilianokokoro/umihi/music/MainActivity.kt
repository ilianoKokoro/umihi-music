package ca.ilianokokoro.umihi.music

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.YoutubeExtractor
import ca.ilianokokoro.umihi.music.core.helpers.YoutubeHelper
import ca.ilianokokoro.umihi.music.core.managers.VersionManager
import ca.ilianokokoro.umihi.music.data.repositories.SongRepository
import ca.ilianokokoro.umihi.music.extensions.playSong
import ca.ilianokokoro.umihi.music.services.PlaybackService
import ca.ilianokokoro.umihi.music.ui.components.dialog.UpdateDialog
import ca.ilianokokoro.umihi.music.ui.navigation.NavigationRoot
import ca.ilianokokoro.umihi.music.ui.theme.UmihiMusicTheme
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.schabi.newpipe.extractor.NewPipe


class MainActivity : ComponentActivity() {
    private var controllerFuture: ListenableFuture<MediaController>? = null

    private val songRepository: SongRepository = SongRepository()
    private lateinit var player: Player

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initCoaoc()

        initNewPipe()

        VersionManager.initialize(this)

        requestNotificationPermission()

        initExoplayer { readyPlayer ->
            player = readyPlayer
            enableEdgeToEdge()
            setContent {
                UmihiMusicTheme {
                    NavigationRoot(
                        modifier = Modifier.fillMaxSize(),
                        player = player
                    )
                    UpdateDialog(lifecycleScope)
                }
            }

            handleShareIntent(intent)
            handleViewIntent(intent)
            checkForUpdate()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
        handleViewIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        if (intent.type != "text/plain") return

        val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return

        val urlRegex = Regex(
            Constants.YoutubeApi.URL_REGEX
        )

        val url = urlRegex.find(text)?.value ?: return

        val videoId = YoutubeHelper.extractYouTubeVideoId(url) ?: return

        playVideoFromId(videoId)
    }

    private fun handleViewIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_VIEW) return
        val data: Uri = intent.data ?: return

        val videoId = YoutubeHelper.extractYouTubeVideoId(data.toString()) ?: return
        playVideoFromId(videoId)
    }


    private fun playVideoFromId(id: String) {
        lifecycleScope.launch {
            songRepository.getSongInfo(id).collect { apiResult ->
                when (apiResult) {
                    is ApiResult.Error -> {
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.get_song_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    ApiResult.Loading -> {}
                    is ApiResult.Success -> {
                        player.playSong(apiResult.data)
                    }
                }
            }
        }

    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun initNewPipe() {
        NewPipe.init(YoutubeExtractor(OkHttpClient()))
    }

    private fun initCoaoc() {
        CaocConfig.Builder.create()
            .backgroundMode(CaocConfig.BACKGROUND_MODE_CRASH)
            .trackActivities(true)
            .apply()
    }

    @OptIn(UnstableApi::class)
    private fun initExoplayer(onReady: (Player) -> Unit) {
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture!!.addListener({
            onReady(controllerFuture!!.get())
        }, MoreExecutors.directExecutor())
    }

    private fun checkForUpdate() {
        lifecycleScope.launch {
            VersionManager.checkForUpdates(this@MainActivity)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
    }
}
