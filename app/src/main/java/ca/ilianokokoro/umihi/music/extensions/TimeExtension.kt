package ca.ilianokokoro.umihi.music.extensions

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

fun Int.toTimeString(): String = seconds.toClockString()

fun Long.toTimeString(): String = seconds.toClockString()

fun Float.toTimeString(): String = toInt().milliseconds.toClockString()

private fun Duration.toClockString(): String = toComponents { hours, minutes, seconds, _ ->
    val secondsPart = seconds.toString().padStart(2, '0')
    if (hours > 0) {
        "$hours:${minutes.toString().padStart(2, '0')}:$secondsPart"
    } else {
        "$minutes:$secondsPart"
    }
}
