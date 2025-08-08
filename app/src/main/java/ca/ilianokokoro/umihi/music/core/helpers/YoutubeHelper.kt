package ca.ilianokokoro.umihi.music.core.helpers

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object YoutubeHelper {
    fun getBestThumbnailUrl(thumbnailElement: JsonElement): String {
        val url =
            thumbnailElement.jsonObject["musicThumbnailRenderer"]?.jsonObject?.get("thumbnail")?.jsonObject?.get(
                "thumbnails"
            )?.jsonArray?.last()?.jsonObject?.get("url")?.jsonPrimitive?.content ?: ""
        return url
    }
}