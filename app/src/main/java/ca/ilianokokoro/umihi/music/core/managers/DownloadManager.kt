package ca.ilianokokoro.umihi.music.core.managers

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.core.net.toUri

class AndroidDownloader(
    context: Context
) {
    private val downloadManager = context.getSystemService(DownloadManager::class.java)

    fun downloadFile(url: String, fileName: String): Long {
        val request = DownloadManager.Request(url.toUri())
            // .setMimeType() ???
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI) // TODO : Maybe both ?
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(fileName)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        return downloadManager.enqueue(request)
    }

    fun downloadUpdate(url: String): Long {
        val fileName = "UmihiMusic.apk"
        return downloadFile(url, fileName)
    }
}