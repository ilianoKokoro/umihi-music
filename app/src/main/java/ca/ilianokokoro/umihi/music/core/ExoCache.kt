package ca.ilianokokoro.umihi.music.core

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File

@OptIn(UnstableApi::class)
class ExoCache private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val cacheDir = File(appContext.cacheDir, Constants.Cache.Audio.DIRECTORY)
    private val databaseProvider by lazy { StandaloneDatabaseProvider(appContext) }

    val cache: SimpleCache by lazy {
        SimpleCache(cacheDir, LeastRecentlyUsedCacheEvictor(getCacheSize()), databaseProvider)
    }

    private fun getCacheSize(): Long {
        val sizeMB = runBlocking {
            DatastoreRepository(appContext).settings.first().exoPlayerCacheSizeMB
        }

        return sizeMB.toLong() * 1024L * 1024L
    }

    fun clear() {
        SimpleCache.delete(cacheDir, databaseProvider)
    }

    fun release() {
        cache.release()
        clearInstance(this)
    }

    companion object {
        @Volatile
        @SuppressLint("StaticFieldLeak")
        private var instance: ExoCache? = null

        fun getInstance(context: Context): ExoCache {
            instance?.let { return it }
            return synchronized(this) {
                instance ?: ExoCache(context).also { instance = it }
            }
        }

        private fun clearInstance(released: ExoCache) {
            synchronized(this) {
                if (instance === released) {
                    instance = null
                }
            }
        }
    }
}