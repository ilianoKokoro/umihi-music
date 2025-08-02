package ca.ilianokokoro.umihi.music.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ProfileScreen() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        innerPadding
        Text("ProfileScreen")
    }
}