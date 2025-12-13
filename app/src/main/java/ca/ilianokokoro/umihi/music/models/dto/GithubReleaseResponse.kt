package ca.ilianokokoro.umihi.music.models.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubReleaseResponse(
    @SerialName("tag_name")
    val tagName: String,
    val body: String,
    val assets: List<AssetsResponse>,
) {
    val versionName: String
        get() = tagName.removePrefix("v")
}

@Serializable
data class AssetsResponse(
    @SerialName("browser_download_url")
    val url: String,
)
