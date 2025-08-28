package ca.ilianokokoro.umihi.music.extensions

fun CharSequence?.toStringOrEmpty(): String {
    if (this == null) {
        return ""
    }
    return this.toString()
}