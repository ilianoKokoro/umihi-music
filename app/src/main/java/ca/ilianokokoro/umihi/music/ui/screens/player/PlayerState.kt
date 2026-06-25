package ca.ilianokokoro.umihi.music.ui.screens.player

import ca.ilianokokoro.umihi.music.models.Song


data class PlayerState(
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val playbackProgress: PlaybackProgress = PlaybackProgress(),
    val isPlaying: Boolean = false,
    val isLoading: Boolean = true,
    val isSeekBarHeld: Boolean = false,
    val isQueueModalShown: Boolean = false,
    val isSleepTimerModalShown: Boolean = false,
    val sleepTimerRemainingSeconds: Long? = null,
    val isSpeedSelectorShown: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val isLoggedIn: Boolean = false,
    val isLiked: Boolean = false,
    val isLiking: Boolean = false,
)

data class PlaybackProgress(
    val position: Float = 0f,
    val duration: Float = 0f,
)