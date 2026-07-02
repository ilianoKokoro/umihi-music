package ca.ilianokokoro.umihi.music.core.exceptions

class GithubRateLimitException(
    val retryAfterSeconds: Long,
    message: String,
) : Exception(message)