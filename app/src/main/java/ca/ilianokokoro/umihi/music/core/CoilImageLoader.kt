package ca.ilianokokoro.umihi.music.core

import android.content.Context
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.request.crossfade
import okio.Path.Companion.toPath
import java.io.File

object CoilImageLoader {
    private const val IMAGE_CACHE_FOLDER = Constants.Downloads.THUMBNAILS_FOLDER

    @Volatile
    private var imageLoader: ImageLoader? = null

    fun get(context: Context): ImageLoader {
        return imageLoader ?: synchronized(this) {
            imageLoader ?: buildImageLoader(context).also { imageLoader = it }
        }
    }

    private fun buildImageLoader(context: Context): ImageLoader {
        val cacheDir = File(context.cacheDir, IMAGE_CACHE_FOLDER)
        val cacheSizeBytes = getCacheSize(context) * 1024L * 1024L

        val diskCache = DiskCache.Builder()
            .directory(cacheDir.absolutePath.toPath())
            .maxSizeBytes(cacheSizeBytes)
            .build()

        return ImageLoader.Builder(context)
            .diskCache(diskCache)
            .crossfade(Constants.Animation.IMAGE_FADE_DURATION)
            .build()
    }

    private fun getCacheSize(context: Context): Int {
        return try {
            kotlinx.coroutines.runBlocking {
                ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository(context).getSettings().thumbnailCacheSizeMB
            }
        } catch (_: Exception) {
            Constants.Cache.Thumbnail.DEFAULT_SIZE_MB
        }
    }

    fun clear(context: Context) {
        synchronized(this) {
            imageLoader?.shutdown()
            imageLoader = null
            File(context.cacheDir, IMAGE_CACHE_FOLDER).deleteRecursively()
        }
    }

    fun reset(context: Context) {
        synchronized(this) {
            imageLoader?.shutdown()
            imageLoader = buildImageLoader(context)
        }
    }
}
