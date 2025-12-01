package ca.ilianokokoro.umihi.music.core.managers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printe
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.Song
import kotlin.math.abs

object UmihiNotificationManager {
    private lateinit var notificationManager: NotificationManager

    fun init(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            NotificationChannels.entries.forEach {
                val notificationChannel = NotificationChannel(
                    it.channelId,
                    it.channelName,
                    it.importance
                ).apply {
                    description = it.description
                }
                notificationManager.createNotificationChannel(notificationChannel)
            }
        } else {
            printe("Could not start the notification channels because the android version is too old")
        }
    }

    fun showPlaylistDownloadProgress(
        context: Context,
        playlist: Playlist,
        currentSong: Int,
        totalSongs: Int
    ) {
        val notification = NotificationCompat.Builder(
            context,
            NotificationChannels.PLAYLIST_DOWNLOAD.channelId
        )
            .setContentTitle(playlist.info.title)
            .setContentText("$currentSong of $totalSongs songs downloaded")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(totalSongs, currentSong, false)
            .setOngoing(true)
            .setGroup(NotificationChannels.PLAYLIST_DOWNLOAD.group)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        notificationManager.notify(getNotificationID(playlist.info.id), notification)
        updateGroupSummary(context)
    }

    fun showPlaylistDownloadSuccess(
        context: Context,
        playlist: Playlist,
    ) {
        val notification = NotificationCompat.Builder(
            context,
            NotificationChannels.PLAYLIST_DOWNLOAD.channelId
        )
            .setContentTitle(playlist.info.title)
            .setContentText("${playlist.info.title} downloaded")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .setGroup(NotificationChannels.PLAYLIST_DOWNLOAD.group)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(getNotificationID(playlist.info.id), notification)
        updateGroupSummary(context)
    }

    fun showPlaylistDownloadFailure(
        context: Context,
        playlist: Playlist,
    ) {
        val notification = NotificationCompat.Builder(
            context,
            NotificationChannels.PLAYLIST_DOWNLOAD.channelId
        )
            .setContentTitle("Download failed")
            .setContentText("Failed to download ${playlist.info.title}")
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setAutoCancel(true)
            .setGroup(NotificationChannels.PLAYLIST_DOWNLOAD.group)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(getNotificationID(playlist.info.id), notification)
        updateGroupSummary(context)
    }

    private fun updateGroupSummary(context: Context) {
        val summaryNotification = NotificationCompat.Builder(
            context,
            NotificationChannels.PLAYLIST_DOWNLOAD.channelId
        )
            .setContentTitle("Playlist Downloads")
            .setContentText("")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setGroup(NotificationChannels.PLAYLIST_DOWNLOAD.group)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        notificationManager.notify(0, summaryNotification)
    }


    fun showSongDownloadFailed(
        context: Context,
        song: Song,
    ) {
        val notification = NotificationCompat.Builder(
            context,
            NotificationChannels.SONG_DOWNLOADS.channelId
        )
            .setContentTitle("Download failed")
            .setContentText("Failed to download ${song.title} - ${song.artist}")
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setAutoCancel(true)
            .setGroup(NotificationChannels.SONG_DOWNLOADS.group)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(getNotificationID(song.youtubeId), notification)
        updateGroupSummary(context)
    }

    private fun getNotificationID(id: String): Int {
        return 1000 + abs(id.hashCode() % 1000)
    }

    private enum class NotificationChannels(
        val channelId: String,
        val channelName: String,
        val description: String,
        val importance: Int,
        val group: String
    ) {
        PLAYLIST_DOWNLOAD(
            channelId = "playlist_download",
            channelName = "Playlist Downloads",
            description = "Shows progress of playlist downloads",
            importance = NotificationManager.IMPORTANCE_LOW,
            group = "PLAYLIST_GROUP"
        ),
        SONG_DOWNLOADS(
            channelId = "playlist_download",
            channelName = "Song downloads",
            description = "Alerts about song downloading",
            importance = NotificationManager.IMPORTANCE_HIGH,
            group = "SONG_GROUP"
        );
    }
}