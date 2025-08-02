package ca.ilianokokoro.umihi.music

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import ca.ilianokokoro.umihi.music.ui.navigation.NavigationRoot
import ca.ilianokokoro.umihi.music.ui.theme.UmihiMusicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UmihiMusicTheme {
                NavigationRoot(
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
    }
}