package ca.ilianokokoro.umihi.music.models

data class Cookies(val raw: String) {
    val data: Map<String, String> by lazy {
        raw.split(";")
            .mapNotNull { entry ->
                val parts = entry.trim().split("=", limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else null
            }
            .toMap()
    }


    fun isEmpty(): Boolean {
        return raw == ""
    }

    fun toRawCookie(): String = raw
}