package ca.ilianokokoro.umihi.music.core.managers

import android.app.Activity
import android.view.WindowManager
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.lang.ref.WeakReference

object ScreenAwakeManager {

    private var currentActivity: WeakReference<Activity>? = null
    private var datastoreRepository: WeakReference<DatastoreRepository>? = null


    fun registerActivity(activity: Activity) {
        currentActivity = WeakReference(activity)

        if (datastoreRepository?.get() == null) {
            datastoreRepository =
                WeakReference(DatastoreRepository(activity.applicationContext))
        }

        val savedSettings = runBlocking { datastoreRepository?.get()?.settings?.first() }
        if (savedSettings != null) {
            setKeepScreenOn(savedSettings.keepScreenOn)
        }
    }

    fun unregisterActivity(activity: Activity) {
        val current = currentActivity?.get()

        if (current == activity) {
            currentActivity?.clear()
            currentActivity = null
        }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        val activity = currentActivity?.get() ?: return

        if (enabled) {
            activity.window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        } else {
            activity.window.clearFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }
}