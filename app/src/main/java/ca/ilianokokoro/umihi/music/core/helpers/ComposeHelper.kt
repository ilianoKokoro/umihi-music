package ca.ilianokokoro.umihi.music.core.helpers

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

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

}
