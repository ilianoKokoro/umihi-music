package ca.ilianokokoro.umihi.music.extensions

import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

fun Float.toTimeString(): String {
    val duration = this.toInt().milliseconds
    return duration.toComponents { hours, minutes, seconds, _ ->
        if (hours > 0) {
            String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
        }
    }
}
