package ca.ilianokokoro.umihi.music.core.helpers

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

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
}
