package ca.ilianokokoro.umihi.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
fun FadingStatusBarWrapper(
    modifier: Modifier = Modifier,
    fadeColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable (statusBarHeight: Dp) -> Unit
) {
    val density = LocalDensity.current

    val statusBarHeight = with(density) {
        WindowInsets.statusBars.getTop(this).toDp()
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        content(statusBarHeight)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(statusBarHeight)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            fadeColor,
                            Color.Transparent
                        )
                    )
                )
        )
    }
}