package ca.ilianokokoro.umihi.music

import android.app.Application
import ca.ilianokokoro.umihi.music.core.CoilImageLoader
import ca.ilianokokoro.umihi.music.core.managers.NotificationManager
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader

class Umihi : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()

        NotificationManager.init(this)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return CoilImageLoader.get(context)
    }
}