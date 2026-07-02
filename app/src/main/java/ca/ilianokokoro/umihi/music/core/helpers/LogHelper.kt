package ca.ilianokokoro.umihi.music.core.helpers

import android.util.Log
import kotlin.time.measureTimedValue

object LogHelper {
    const val TAG = "UmihiPrint"

    inline fun <T> benchmark(
        label: String,
        block: () -> T
    ): T {
        val result = measureTimedValue(block)
        printd(tag = "UmihiBench", message = "$label: ${result.duration.inWholeMilliseconds} ms")
        return result.value
    }

    fun printd(message: String, tag: String = TAG) {
        Log.d(tag, message)
    }

    fun printe(message: String, tag: String = TAG, exception: Exception? = null) {
        Log.e(TAG, message, exception)
    }
}
