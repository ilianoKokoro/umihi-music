package ca.ilianokokoro.umihi.music

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.DiagnosticLog
import ca.ilianokokoro.umihi.music.core.managers.PlayerManager
import ca.ilianokokoro.umihi.music.core.managers.VersionManager
import ca.ilianokokoro.umihi.music.core.youtube.YoutubeDataExtractor
import ca.ilianokokoro.umihi.music.core.youtube.YoutubeExtractor
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import ca.ilianokokoro.umihi.music.data.repositories.SongRepository
import ca.ilianokokoro.umihi.music.models.enums.ThemeMode
import ca.ilianokokoro.umihi.music.ui.components.dialog.DatabaseDestructionDialog
import ca.ilianokokoro.umihi.music.ui.components.dialog.UpdateDialog
import ca.ilianokokoro.umihi.music.ui.navigation.NavigationRoot
import ca.ilianokokoro.umihi.music.ui.theme.UmihiMusicTheme
import cat.ereza.customactivityoncrash.config.CaocConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.schabi.newpipe.extractor.NewPipe

class MainActivity : ComponentActivity() {
    private val songRepository: SongRepository = SongRepository()
    private val datastoreRepository by lazy {
        DatastoreRepository(applicationContext)
    }
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}


    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        initCaoc()

        enableEdgeToEdge()
        setContent {
            val settings by datastoreRepository.settings.collectAsStateWithLifecycle(initialValue = null)
            val isDarkTheme = when (settings?.themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                else -> isSystemInDarkTheme()
            }

            UmihiMusicTheme(darkTheme = isDarkTheme) {
                NavigationRoot(
                    modifier = Modifier.fillMaxSize()
                )
                DatabaseDestructionDialog()
                UpdateDialog(lifecycleScope)

            }
        }


        observeKeepScreenOn()
        VersionManager.initialize(this)
        initNewPipe()

        requestNotificationPermission()

        if (savedInstanceState == null) {
            handleShareIntent(intent)
            handleViewIntent(intent)
            checkForUpdate()
            DiagnosticLog.initialize(this)
        }

        //    debugPrintDataStores()
    }

//    private fun debugPrintDataStores() {
//        lifecycleScope.launch {
//            DatastoreRepository(this@MainActivity).debugPrintAllPreferences()
//        }
//    }


    override fun onStart() {
        super.onStart()
        PlayerManager.connectController(this)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShareIntent(intent)
        handleViewIntent(intent)
    }

    private fun observeKeepScreenOn() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                datastoreRepository.settings.collect { settings ->
                    window.setKeepScreenOn(settings.keepScreenOn)
                }
            }
        }
    }

    private fun Window.setKeepScreenOn(enabled: Boolean) {
        if (enabled) {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val useSpecialLanguage = runBlocking {
            DatastoreRepository(newBase).settings.first().useSpecialLanguage
        }

        val context = if (useSpecialLanguage) {
            val config = Configuration(newBase.resources.configuration)
            @Suppress("UsePropertyAccessSyntax") // (From the description of setLocales) This is the preferred way for setting up the locales (instead of using the direct accessor or setLocale
            config.setLocales(LocaleList.forLanguageTags(Constants.Locale.Special.CODE))
            newBase.createConfigurationContext(config)
        } else {
            newBase
        }

        super.attachBaseContext(context)
    }

    private fun handleShareIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) {
            return
        }
        if (intent.type != "text/plain") {
            return
        }

        val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return

        val urlRegex = Regex(
            Constants.YoutubeApi.URL_REGEX
        )

        val url = urlRegex.find(text)?.value ?: return

        val videoId = YoutubeDataExtractor.extractYouTubeVideoId(url) ?: return

        playVideoFromId(videoId)
    }

    private fun handleViewIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_VIEW) {
            return
        }
        val data: Uri = intent.data ?: return

        val videoId = YoutubeDataExtractor.extractYouTubeVideoId(data.toString()) ?: return
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
                        PlayerManager.playSong(apiResult.data)
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
        NewPipe.init(YoutubeExtractor())
    }

    private fun initCaoc() {
        CaocConfig.Builder.create()
            .backgroundMode(CaocConfig.BACKGROUND_MODE_CRASH)
            .trackActivities(true)
            .apply()
    }

    private fun checkForUpdate() {
        lifecycleScope.launch {
            VersionManager.checkForUpdates(this@MainActivity)
        }
    }
}
