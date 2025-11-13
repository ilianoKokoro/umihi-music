package ca.ilianokokoro.umihi.music.data.datasources

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class GithubDatasource {
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class ReleaseResponse(
        val tag_name: String
    )

    fun getLatestVersionName(): String {
// TODO constants
        val (_, _, result) = "https://api.github.com/repos/ilianoKokoro/umihi-music/releases/latest".httpGet()
            .responseJson()

        return when (result) {
            is Result.Success -> {
                val release = json.decodeFromString<ReleaseResponse>(result.value.content)
                release.tag_name
            }

            is Result.Failure -> {
                throw Exception("Failed to get the latest GitHub release version name")
            }
        }
    }
}