package ca.ilianokokoro.umihi.music.core.helpers

import android.util.Log
import ca.ilianokokoro.umihi.music.core.DiagnosticLog

object LogHelper {
    const val TAG = "UmihiPrint"

    fun printd(message: String, tag: String = TAG) {
        Log.d(tag, message)
        DiagnosticLog.write("DEBUG", message)
    }

    fun printe(message: String, tag: String = TAG, exception: Exception? = null) {
        Log.e(tag, message, exception)
        DiagnosticLog.write("ERROR", message)
    }
}
