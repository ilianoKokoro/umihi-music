package ca.ilianokokoro.umihi.music.models

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT;

    companion object {
        fun fromString(value: String): ThemeMode {
            return entries.first { it.name.equals(value, ignoreCase = true) }
        }
    }
}
