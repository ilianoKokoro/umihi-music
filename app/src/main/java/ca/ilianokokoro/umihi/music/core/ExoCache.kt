package ca.ilianokokoro.umihi.music.core

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import kotlinx.coroutines.flow.first
import java.io.File

@UnstableApi
class ExoCache(private val context: Context) {
    private val cacheDir = File(context.cacheDir, Constants.Cache.Audio.DIRECTORY)

    fun getCacheSize(): Long {
        val sizeMB = try {
            kotlinx.coroutines.runBlocking {
                DatastoreRepository(context).settings.first().exoPlayerCacheSizeMB
            }
        } catch (_: Exception) {
            Constants.Cache.Audio.DEFAULT_SIZE_MB
        }
        return sizeMB.toLong() * 1024L * 1024L
    }

    val cache: SimpleCache by lazy {
        val cacheSize = getCacheSize()
        val cacheEvictor = LeastRecentlyUsedCacheEvictor(cacheSize)
        SimpleCache(
            cacheDir,
            cacheEvictor,
            databaseProvider
        )
    }

    fun clear() {
        SimpleCache.delete(cacheDir, databaseProvider)
    }

    fun release() {
        cache.release()
    }

    private val databaseProvider by lazy { StandaloneDatabaseProvider(context) }
}
