package ca.ilianokokoro.umihi.music.core.datasources

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.ResolvingDataSource
import ca.ilianokokoro.umihi.music.core.helpers.YoutubeHelper
import kotlinx.coroutines.runBlocking
import java.io.File

@UnstableApi
class YoutubeDataSourceFactory(
    private val application: Application,
    private val cacheDataSourceFactory: DataSource.Factory
) : DataSource.Factory {

    override fun createDataSource(): DataSource {
        return ResolvingDataSource(cacheDataSourceFactory.createDataSource()) { dataSpec ->
            dataSpec.withUri(resolveUri(dataSpec.uri))
        }
    }

    private fun resolveUri(uri: Uri): Uri {
        if (uri.scheme != null) return uri

        val streamUri = runBlocking {
            YoutubeHelper.getSongPlayerUrl(
                context = application,
                songId = uri.toString(),
                allowLocal = true
            )
        }

        return if (streamUri.startsWith("/")) Uri.fromFile(File(streamUri))
        else streamUri.toUri()
    }
}