package ca.ilianokokoro.umihi.music

import android.app.Application
import ca.ilianokokoro.umihi.music.core.UmihiHttpClient
import ca.ilianokokoro.umihi.music.core.managers.UmihiNotificationManager
import com.github.kittinunf.fuel.core.FuelManager

class Umihi : Application() {

    override fun onCreate() {
        super.onCreate()

        FuelManager.instance.apply {
            client = UmihiHttpClient.client
        }
        
        UmihiNotificationManager.init(this)
    }

}