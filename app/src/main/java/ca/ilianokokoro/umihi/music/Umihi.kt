package ca.ilianokokoro.umihi.music

import android.app.Application
import ca.ilianokokoro.umihi.music.core.managers.NotificationManager

class Umihi : Application() {

    override fun onCreate() {
        super.onCreate()

        NotificationManager.init(this)
    }

}