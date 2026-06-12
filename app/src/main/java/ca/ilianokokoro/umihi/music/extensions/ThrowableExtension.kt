package ca.ilianokokoro.umihi.music.extensions


fun Throwable?.toException(): Exception {
    return Exception(this?.message, this)
}