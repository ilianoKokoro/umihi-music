package ca.ilianokokoro.umihi.music.error

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ca.ilianokokoro.umihi.music.ui.theme.UmihiMusicTheme

class ErrorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UmihiMusicTheme {
                ErrorScreen()
            }
        }

    }
}