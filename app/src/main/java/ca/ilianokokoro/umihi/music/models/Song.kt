package ca.ilianokokoro.umihi.music.models

import kotlinx.serialization.Serializable

@Serializable
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val lowQualityCoverHref: String
)