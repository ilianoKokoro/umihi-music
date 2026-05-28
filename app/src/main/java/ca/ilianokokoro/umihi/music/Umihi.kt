package ca.ilianokokoro.umihi.music

import android.app.Application
import ca.ilianokokoro.umihi.music.core.managers.UmihiNotificationManager

class Umihi : Application() {

    override fun onCreate() {
        super.onCreate()

        UmihiNotificationManager.init(this)
    }

}