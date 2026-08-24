package ca.ilianokokoro.umihi.music.models

import androidx.compose.runtime.Immutable

sealed interface HomeSectionItem {
    data class SongItem(val song: Song, val rank: Int? = null) : HomeSectionItem
    data class PlaylistItem(val playlistInfo: PlaylistInfo) : HomeSectionItem
    data class ArtistItem(val name: String, val thumbnailHref: String?, val songCount: Int = 0) : HomeSectionItem
}

@Immutable
data class HomeSection(
    val id: String = "",
    val title: String,
    val subtitle: String? = null,
    val items: List<HomeSectionItem> = emptyList(),
) {
    val songs: List<Song>
        get() = items.filterIsInstance<HomeSectionItem.SongItem>().map { it.song }

    val playlists: List<PlaylistInfo>
        get() = items.filterIsInstance<HomeSectionItem.PlaylistItem>().map { it.playlistInfo }

    val isSongSection: Boolean
        get() = items.isNotEmpty() && items.all { it is HomeSectionItem.SongItem }
}
