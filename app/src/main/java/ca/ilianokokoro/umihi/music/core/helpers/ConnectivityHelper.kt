package ca.ilianokokoro.umihi.music.core.helpers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object ConnectivityHelper {
    fun isMeteredNetwork(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
    }
}
