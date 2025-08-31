package ca.ilianokokoro.umihi.music.ui.screens.player

import ca.ilianokokoro.umihi.music.models.Song


data class PlayerState(
    val queue: List<Song> = listOf(),
    val currentIndex: Int = -1,
    val progressMs: Float = 0f,
    val durationMs: Float = 0f,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = true,
    val isSeekBarHeld: Boolean = false,
    val isQueueModalShown: Boolean = false,
)