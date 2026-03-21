package ca.ilianokokoro.umihi.music.core.helpers

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.media3.common.Player

object ComposeHelper {

    fun <T : Any> getLazyKey(element: T, id: String, index: Int): String {
        return "${element::class}_${id}_${index}"
    }

    fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    fun String.getShortErrorFromLog(): String {
        return this
            .lineSequence()
            .take(7)
            .joinToString("\n")
    }

    @Composable
    fun rememberInteractionSource(): MutableInteractionSource {
        return remember { MutableInteractionSource() }
    }

    fun String.getBulletPointsFromMarkdown(): String =
        lineSequence()
            .map { it.trimStart() }
            .filter { it.startsWith("-") }
            .joinToString("\n") { "• " + it.removePrefix("-").trimStart() }

    @Composable
    fun rememberRepeatMode(player: Player?): Int {
        val repeatModeState =
            remember { mutableIntStateOf(player?.repeatMode ?: Player.REPEAT_MODE_OFF) }

        DisposableEffect(player) {
            if (player == null) return@DisposableEffect onDispose {}

            val listener = object : Player.Listener {
                override fun onRepeatModeChanged(repeatMode: Int) {
                    repeatModeState.intValue = repeatMode
                }
            }

            player.addListener(listener)

            onDispose {
                player.removeListener(listener)
            }
        }

        return repeatModeState.intValue
    }

}
