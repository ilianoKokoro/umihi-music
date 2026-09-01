package ca.ilianokokoro.umihi.music.extensions

import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.safeObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

fun JsonObject.getClientName(): String {
    return this.safeObject()?.get("clientName")?.jsonPrimitive?.contentOrNull ?: ""
}