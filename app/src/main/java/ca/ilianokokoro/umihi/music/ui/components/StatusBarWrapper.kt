package ca.ilianokokoro.umihi.music.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource

@Composable
fun StatusBarWrapper(
    modifier: Modifier = Modifier,
    content: @Composable (statusBarHeight: Dp) -> Unit
) {
    val density = LocalDensity.current
    val hazeState = remember { HazeState() }

    val statusBarHeight = with(density) {
        WindowInsets.statusBars.getTop(this).toDp()
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
        ) {
            content(statusBarHeight)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(statusBarHeight * 1.1f)
                .align(Alignment.TopCenter)
                .hazeBlur(
                    input = HazeInput.Sources(hazeState),
                    style = HazeBlurStyle {
                        blurRadius(16.dp)
                        progressive(
                            HazeProgressive.verticalGradient(
                                startIntensity = 1f,
                                endIntensity = 0f,
                            )
                        )
                    },
                )
        )
    }
}