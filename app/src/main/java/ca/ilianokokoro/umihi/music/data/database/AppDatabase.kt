package ca.ilianokokoro.umihi.music.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.data.datasources.local.LocalPlaylistDataSource
import ca.ilianokokoro.umihi.music.data.datasources.local.LocalSongDataSource
import ca.ilianokokoro.umihi.music.data.datasources.local.VersionDataSource
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.PlaylistSongCrossRef
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.models.Version
import java.util.concurrent.Executors

@Database(
    entities = [Song::class, PlaylistInfo::class, PlaylistSongCrossRef::class, Version::class],
    version = Constants.Database.VERSION,
    exportSchema = false // Set to true to get an exported json
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songRepository(): LocalSongDataSource
    abstract fun playlistRepository(): LocalPlaylistDataSource
    abstract fun versionRepository(): VersionDataSource

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        suspend fun clearDownloads(context: Context) {
            val instance = getInstance(context)

            instance.songRepository().deleteAll()
            instance.playlistRepository().deleteAll()
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, Constants.Database.NAME
            )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()

        /**
         * Utility method to run blocks on a dedicated background thread, used for io/database work.
         */
        private val IO_EXECUTOR = Executors.newSingleThreadExecutor()
        fun ioThread(f: () -> Unit) {
            IO_EXECUTOR.execute(f)
        }
    }
}