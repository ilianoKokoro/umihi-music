package ca.ilianokokoro.umihi.music.core.helpers

import android.util.Log


object UmihiHelper {
    const val TAG = "UmihiPrint"

    fun printd(message: String, tag: String = TAG) {
        Log.d(tag, message)
    }

    fun printe(message: String, tag: String = TAG, exception: Exception? = null) {
        Log.e(TAG, message, exception)
    }
}
