package ca.ilianokokoro.umihi.music.core.factories

import android.app.Application
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy

@UnstableApi
class YoutubeMediaSourceFactory(
    private val application: Application,
    private val dataSourceFactory: DataSource.Factory
) : MediaSource.Factory {

    override fun createMediaSource(mediaItem: MediaItem): MediaSource {
        val interceptingFactory = YoutubeDataSourceFactory(application, dataSourceFactory)


        
        return ProgressiveMediaSource.Factory(interceptingFactory)
            .createMediaSource(mediaItem)
    }

    override fun setDrmSessionManagerProvider(
        drmSessionManagerProvider: DrmSessionManagerProvider
    ): MediaSource.Factory {
        return this
    }

    override fun setLoadErrorHandlingPolicy(
        loadErrorHandlingPolicy: LoadErrorHandlingPolicy
    ): MediaSource.Factory {
        return this
    }

    override fun getSupportedTypes(): IntArray {
        return intArrayOf(C.AUDIO_CONTENT_TYPE_MUSIC)
    }
}

