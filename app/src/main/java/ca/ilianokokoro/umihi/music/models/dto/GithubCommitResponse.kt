package ca.ilianokokoro.umihi.music.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class GithubCommitResponse(
    val sha: String,
    val commit: CommitResponse
)


@Serializable
data class CommitResponse(
    val message: String,
)

