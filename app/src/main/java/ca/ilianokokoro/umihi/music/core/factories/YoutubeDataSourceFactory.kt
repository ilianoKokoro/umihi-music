package ca.ilianokokoro.umihi.music.core.factories

import android.app.Application
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import ca.ilianokokoro.umihi.music.core.helpers.YoutubeHelper
import kotlinx.coroutines.runBlocking

@UnstableApi
class YoutubeDataSourceFactory(
    private val application: Application,
    private val baseFactory: DataSource.Factory
) : DataSource.Factory {
    override fun createDataSource(): DataSource {
        val base = baseFactory.createDataSource()
        return object : DataSource by base {
            override fun open(dataSpec: DataSpec): Long {
                val actualUri = if (dataSpec.uri.scheme == null) {
                    runBlocking {
                        YoutubeHelper.getSongPlayerUrl(application, dataSpec.uri.toString()).toUri()
                    }
                } else {
                    dataSpec.uri
                }

                val newSpec = dataSpec.withUri(actualUri)
                return base.open(newSpec)
            }

            override fun getResponseHeaders(): Map<String, List<String>> {
                return base.responseHeaders
            }
        }
    }
}