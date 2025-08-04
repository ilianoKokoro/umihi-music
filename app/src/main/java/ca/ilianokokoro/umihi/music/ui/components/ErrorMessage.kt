package ca.ilianokokoro.umihi.music.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun ErrorMessage(
    ex: Exception,
    errorMessage: String? = null,
    onRetry: () -> Unit
) {
    val message = errorMessage ?: ex.localizedMessage

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = message, color = Color.Red, fontSize = 14.sp)
        Button(onClick = { onRetry() }) {
            Text("Retry")
        }
    }
}