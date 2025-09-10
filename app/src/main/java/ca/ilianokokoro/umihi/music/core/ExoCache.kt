package ca.ilianokokoro.umihi.music.core

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@UnstableApi
class ExoCache(private val context: Context) {
    private val databaseProvider by lazy { StandaloneDatabaseProvider(context) }
    val cache: SimpleCache by lazy {
        val cacheDir = File(context.cacheDir, Constants.ExoPlayer.Cache.NAME)
        val cacheEvictor = LeastRecentlyUsedCacheEvictor(Constants.ExoPlayer.Cache.SIZE)
        SimpleCache(
            cacheDir,
            cacheEvictor,
            databaseProvider
        )
    }
}
