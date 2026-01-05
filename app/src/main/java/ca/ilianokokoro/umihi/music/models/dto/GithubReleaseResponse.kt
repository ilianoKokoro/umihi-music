package ca.ilianokokoro.umihi.music.models.dto

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.ComposeHelper.getBulletPointsFromMarkdown
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

    val isBeta: Boolean
        get() = !tagName.contains(".")

    val cleanBody: String
        get() = if (isBeta)
            body
        else
            body.getBulletPointsFromMarkdown()

    val downloadUrl: String
        get() = if (isBeta)
            Constants.Url.Github.Beta.DOWNLOAD
        else
            Constants.Url.Github.Release.DOWNLOAD

}

@Serializable
data class AssetsResponse(
    @SerialName("browser_download_url")
    val url: String,
)
