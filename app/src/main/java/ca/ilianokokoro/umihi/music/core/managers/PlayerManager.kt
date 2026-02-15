package ca.ilianokokoro.umihi.music.core.managers

import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import ca.ilianokokoro.umihi.music.services.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

@OptIn(UnstableApi::class)
object PlayerManager {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private val pendingCallbacks = mutableSetOf<(MediaController) -> Unit>()

    val isConnected: Boolean
        get() = controller != null

    val currentController: MediaController?
        get() = controller

    fun init(context: Context) {
        if (controllerFuture != null || controller != null) {
            return
        }

        val appContext = context.applicationContext
        val sessionToken = SessionToken(
            appContext,
            ComponentName(appContext, PlaybackService::class.java)
        )

        controllerFuture = MediaController.Builder(appContext, sessionToken).buildAsync()

        controllerFuture!!.addListener(
            {
                val built = controllerFuture?.get() ?: return@addListener
                controller = built

                val callbacks = pendingCallbacks.toList()
                pendingCallbacks.clear()
                callbacks.forEach { it(built) }
            },
            MoreExecutors.directExecutor()
        )
    }


    fun connectController(
        context: Context,
        onConnected: (MediaController) -> Unit
    ) {
        val existing = controller
        if (existing != null) {
            onConnected(existing)
            return
        }

        pendingCallbacks.add(onConnected)
        init(context)
    }
}
