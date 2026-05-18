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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@OptIn(UnstableApi::class)
object PlayerManager {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _controllerState = MutableStateFlow<MediaController?>(null)
    val controllerState: StateFlow<MediaController?> = _controllerState.asStateFlow()

    val isConnected: Boolean
        get() = controller != null

    val currentController: MediaController?
        get() = controller

    private fun init(context: Context) {
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
                _controllerState.value = built
            },
            MoreExecutors.directExecutor()
        )
    }

    fun connectController(context: Context) {
        if (controller != null) {
            return
        }
        init(context)
    }
}