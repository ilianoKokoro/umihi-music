package ca.ilianokokoro.umihi.music.ui.screens.player

import ca.ilianokokoro.umihi.music.models.Song


data class PlayerState(
    val currentSong: Song = Song("", "", "", ""),
    val progressMs: Float = 0f,
    val durationMs: Float = 0f,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = true,
    val isSeekBarHeld: Boolean = false,
)