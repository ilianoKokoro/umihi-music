package ca.ilianokokoro.umihi.music.core.helpers

import android.content.Context
import android.util.Log
import ca.ilianokokoro.umihi.music.core.Constants
import java.io.File
import java.nio.file.Paths


object UmihiHelper {
    const val TAG = "UmihiPrint"

    fun printd(message: String, tag: String = TAG) {
        Log.d(tag, message)
    }

    fun printe(message: String, tag: String = TAG, exception: Exception? = null) {
        Log.e(TAG, message, exception)
    }

    fun getDownloadDirectory(context: Context, directory: String? = null): File {
        val dir = File(
            context.filesDir,
            if (directory == null)
                Constants.Downloads.DIRECTORY
            else
                Paths.get(Constants.Downloads.DIRECTORY, directory)
                    .toString()
        )
        dir.mkdirs()
        return dir
    }
}
