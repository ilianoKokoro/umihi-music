package ca.ilianokokoro.umihi.music.ui.screens.player

import ca.ilianokokoro.umihi.music.models.Song


data class PlayerState(
    val currentSong: Song = Song("", "", "", ""),
    val progressMs: Long = 0,
    val isPlaying: Boolean = false
)