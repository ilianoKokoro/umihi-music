package ca.ilianokokoro.umihi.music.core.managers

import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.helpers.LogHelper.printe
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.Song
import kotlin.math.abs
import android.app.NotificationManager as AndroidNotificationManager

object NotificationManager {
    private lateinit var androidNotificationManager: AndroidNotificationManager
    private lateinit var pendingIntent: PendingIntent

    fun init(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        androidNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannels.entries.forEach {
                val notificationChannel = NotificationChannel(
                    it.channelId,
                    context.getString(it.nameRes),
                    it.importance
                ).apply {
                    description = context.getString(it.descriptionRes)
                }
                androidNotificationManager.createNotificationChannel(notificationChannel)
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

        val notification = getBaseNotification(context, NotificationChannels.PLAYLIST_DOWNLOAD)
            .setContentTitle(playlist.info.title)
            .setContentText(
                context.getString(
                    R.string.number_of_songs_downloaded,
                    currentSong,
                    totalSongs
                )
            )
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(totalSongs, currentSong, false)
            .setOngoing(true)
            .setGroup(NotificationChannels.PLAYLIST_DOWNLOAD.group)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        androidNotificationManager.notify(getNotificationID(playlist.info.id), notification)
        updateGroupSummary(context)
    }

    fun showPlaylistDownloadSuccess(
        context: Context,
        playlist: Playlist,
    ) {
        val notification = getBaseNotification(context, NotificationChannels.PLAYLIST_DOWNLOAD)
            .setContentTitle(playlist.info.title)
            .setContentText(context.getString(R.string.playlist_downloaded))
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .setGroup(NotificationChannels.PLAYLIST_DOWNLOAD.group)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        androidNotificationManager.notify(getNotificationID(playlist.info.id), notification)
        updateGroupSummary(context)
    }

    fun showPlaylistDownloadFailure(
        context: Context,
        playlist: Playlist,
    ) {
        val notification = getBaseNotification(context, NotificationChannels.PLAYLIST_DOWNLOAD)
            .setContentTitle(context.getString(R.string.download_failed))
            .setContentText(
                context.getString(
                    R.string.failed_to_download_playlist,
                    playlist.info.title
                )
            )
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setAutoCancel(true)
            .setGroup(NotificationChannels.PLAYLIST_DOWNLOAD.group)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        androidNotificationManager.notify(getNotificationID(playlist.info.id), notification)
        updateGroupSummary(context)
    }

    fun showPlaylistDownloadCanceled(
        context: Context,
        playlist: Playlist
    ) {
        val notification = getBaseNotification(context, NotificationChannels.PLAYLIST_DOWNLOAD)
            .setContentTitle(playlist.info.title)
            .setContentText(context.getString(R.string.download_canceled))
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setAutoCancel(true)
            .setGroup(NotificationChannels.PLAYLIST_DOWNLOAD.group)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        androidNotificationManager.notify(getNotificationID(playlist.info.id), notification)
        updateGroupSummary(context)
    }

    private fun updateGroupSummary(context: Context) {
        val summaryNotification =
            getBaseNotification(context, NotificationChannels.PLAYLIST_DOWNLOAD)
                .setContentTitle(context.getString(R.string.download_finished))
                .setContentText(String())
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setGroup(NotificationChannels.PLAYLIST_DOWNLOAD.group)
                .setGroupSummary(true)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

        androidNotificationManager.notify(0, summaryNotification)
    }


    fun showSongDownloadFailed(
        context: Context,
        song: Song,
    ) {
        val notification = getBaseNotification(context, NotificationChannels.SONG_DOWNLOAD)
            .setContentTitle(context.getString(R.string.download_failed))
            .setContentText(
                context.getString(
                    R.string.failed_to_download_song,
                    song.title,
                    song.artist
                )
            )
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setAutoCancel(true)
            .setGroup(NotificationChannels.SONG_DOWNLOAD.group)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        androidNotificationManager.notify(getNotificationID(song.youtubeId), notification)
    }

    suspend fun showSongDownloadSuccess(
        context: Context,
        song: Song,
    ) {
        val notification = getBaseNotification(context, NotificationChannels.SONG_DOWNLOAD)
            .setContentTitle(song.title)
            .setContentText(context.getString(R.string.song_downloaded))
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setLargeIcon(song.getThumbnailBitmap())
            .setAutoCancel(true)
            .setGroup(NotificationChannels.SONG_DOWNLOAD.group)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        androidNotificationManager.notify(getNotificationID(song.youtubeId), notification)
    }

    private fun getBaseNotification(
        context: Context,
        channel: NotificationChannels
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(
            context,
            channel.channelId
        )
            .setContentIntent(pendingIntent)
    }

    private fun getNotificationID(id: String): Int {
        return 1000 + abs(id.hashCode() and 0x7fffffff)
    }


    private enum class NotificationChannels(
        val channelId: String,
        @param:StringRes val nameRes: Int,
        @param:StringRes val descriptionRes: Int,
        val importance: Int,
        val group: String
    ) {

        PLAYLIST_DOWNLOAD(
            channelId = "playlist_progress",
            nameRes = R.string.playlist_progress_name,
            descriptionRes = R.string.playlist_progress_description,
            importance = AndroidNotificationManager.IMPORTANCE_LOW,
            group = "PLAYLIST_GROUP"
        ),

        SONG_DOWNLOAD(
            channelId = "song_alerts",
            nameRes = (R.string.song_alerts_name),
            descriptionRes = (R.string.song_alerts_description),
            importance = AndroidNotificationManager.IMPORTANCE_DEFAULT,
            group = "SONG_GROUP"
        );
    }
}