package ca.ilianokokoro.umihi.music.extensions

import java.net.UnknownHostException


fun Throwable?.toException(): Exception {
    return Exception(this?.message, this)
}


fun Throwable?.isNetworkError(): Boolean {
    var current: Throwable? = this
    while (current != null) {
        if (current is UnknownHostException) {
            return true
        }
        current = current.cause
    }
    return false
}