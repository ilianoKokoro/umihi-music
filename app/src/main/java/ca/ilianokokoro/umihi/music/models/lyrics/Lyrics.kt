package ca.ilianokokoro.umihi.music.models.lyrics

class Lyrics(
    private val lines: List<SyncedLine>,
) {

    val displayLines = lines.map {
        var cleaned = it.copy(text = it.text.trim())
        if (cleaned.text.isBlank()) {
            cleaned = it.copy(text = "…")
        }

        return@map cleaned
    }

    fun indexOfCurrentLine(positionMs: Long): Int {
        var lo = 0
        var hi = lines.size - 1
        var result = -1
        while (lo <= hi) {
            val mid = (lo + hi) / 2
            if (this.lines[mid].timeMs <= positionMs) {
                result = mid
                lo = mid + 1
            } else {
                hi = mid - 1
            }
        }
        return result
    }

}
