package ca.ilianokokoro.umihi.music.models

enum class ThemeMode {
    DARK,
    LIGHT,
    SYSTEM;

    companion object {
        fun fromString(value: String?): ThemeMode {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: DARK
        }
    }
}
