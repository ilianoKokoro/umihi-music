package ca.ilianokokoro.umihi.music.data.datasources

import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.models.Playlist
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import kotlinx.serialization.json.Json

class PlaylistDataSource {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun retrieveAll(): List<Playlist> {

        // TODO 
        val (_, _, result) = Constants.YoutubeApi.Browse.URL.httpPost()
//            .header(
//                Constants.Header.Authorization,
//                format(Constants.Header.Bearer, datastoreRepository.getTokens().accessToken)
//            )
            .responseJson()

        return when (result) {
            is Result.Success -> {
                json.decodeFromString(result.value.content)
            }

            is Result.Failure -> {
                throw result.error.exception
            }
        }
    }
}