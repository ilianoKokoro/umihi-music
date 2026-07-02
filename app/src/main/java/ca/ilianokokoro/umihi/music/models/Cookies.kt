package ca.ilianokokoro.umihi.music.models

import androidx.compose.runtime.Immutable


@Immutable
data class Cookies(val raw: String = String()) {
    val data: Map<String, String> by lazy {
        raw.split(";")
            .mapNotNull { entry ->
                val parts = entry.trim().split("=", limit = 2)
                if (parts.size == 2) {
                    parts[0] to parts[1]
                } else {
                    null
                }
            }
            .toMap()
    }


    fun isEmpty(): Boolean {
        return raw.isBlank()
    }

    fun isNotEmpty(): Boolean {
        return !isEmpty()
    }

    fun toRawCookie(): String = raw
}