package ca.ilianokokoro.umihi.music.ui.components.bottomsheet.addtoplaylist

import ca.ilianokokoro.umihi.music.models.AddToPlaylistOption

data class AddToPlaylistState(
    val screenState: AddToPlaylistScreenState = AddToPlaylistScreenState.Loading,
    val pendingToggles: Set<String> = emptySet(),
    val submitting: Boolean = false,
) {
    val hasPendingChanges: Boolean
        get() = pendingToggles.isNotEmpty()

    fun isChecked(option: AddToPlaylistOption): Boolean =
        option.playlistId in pendingToggles
}

sealed class AddToPlaylistScreenState {
    data object Loading : AddToPlaylistScreenState()
    data class Success(val options: List<AddToPlaylistOption>) : AddToPlaylistScreenState()
    data class Error(val exception: Exception) : AddToPlaylistScreenState()
}

