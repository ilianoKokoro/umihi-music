package ca.ilianokokoro.umihi.music.ui.components.bottomsheet

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.models.AddToPlaylistOption
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.components.ErrorMessage
import ca.ilianokokoro.umihi.music.ui.components.SquareImage
import ca.ilianokokoro.umihi.music.ui.components.materialu.MaterialUButton
import ca.ilianokokoro.umihi.music.ui.components.materialu.MaterialUButtonSize
import ca.ilianokokoro.umihi.music.ui.components.materialu.MaterialUButtonVariant
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistBottomSheet(
    song: Song,
    application: Application,
    onClose: () -> Unit,
    onStateChanged: () -> Unit = {},
) {
    val addToPlaylistViewModel: AddToPlaylistViewModel = viewModel(
        key = "add_to_playlist_${song.youtubeId}",
        factory = AddToPlaylistViewModel.Factory(application),
    )
    val uiState = addToPlaylistViewModel.uiState.collectAsStateWithLifecycle().value

    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    val scope = rememberCoroutineScope()

    var createPlaylistOpen by rememberSaveable { mutableStateOf(false) }

    fun dismiss() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onClose() }
    }

    LaunchedEffect(song.youtubeId) {
        addToPlaylistViewModel.load(song.youtubeId)
    }

    ModalBottomSheet(
        onDismissRequest = { dismiss() },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .aspectRatio(1f)
                ) {
                    SquareImage(
                        uri = song.thumbnailPath ?: song.thumbnailHref,
                        modifier = Modifier.matchParentSize(),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (val screenState = uiState.screenState) {
                is AddToPlaylistScreenState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                }

                is AddToPlaylistScreenState.Error -> {
                    ErrorMessage(
                        ex = screenState.exception,
                        onRetry = { addToPlaylistViewModel.load(song.youtubeId) },
                    )
                }

                is AddToPlaylistScreenState.Success -> {
                    if (uiState.submitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(vertical = 32.dp)
                        )
                    } else {
                        AddToPlaylistCreateRow(
                            onClick = { createPlaylistOpen = true },
                        )
                        if (screenState.options.isEmpty()) {
                            Text(
                                text = stringResource(R.string.no_playlists_found),
                                modifier = Modifier.padding(24.dp),
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 420.dp),
                            ) {
                                items(
                                    items = screenState.options,
                                    key = { option -> option.playlistId },
                                ) { option ->
                                    AddToPlaylistRow(
                                        option = option,
                                        checked = uiState.isChecked(option),
                                        onToggle = {
                                            addToPlaylistViewModel.toggle(option.playlistId)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val showControls = uiState.screenState is AddToPlaylistScreenState.Success ||
                uiState.screenState is AddToPlaylistScreenState.Loading
            val controlsEnabled = showControls && !uiState.submitting

            if (showControls) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                ) {
                    MaterialUButton(
                        onClick = {
                            addToPlaylistViewModel.cancel()
                            dismiss()
                        },
                        text = stringResource(R.string.cancel),
                        size = MaterialUButtonSize.Small,
                        variant = MaterialUButtonVariant.Tonal,
                        enabled = controlsEnabled,
                    )
                    MaterialUButton(
                        onClick = {
                            if (uiState.hasPendingChanges) {
                                addToPlaylistViewModel.confirm(
                                    song = song,
                                    onStateChanged = onStateChanged,
                                    onComplete = { dismiss() },
                                )
                            } else {
                                dismiss()
                            }
                        },
                        text = stringResource(R.string.confirm),
                        size = MaterialUButtonSize.Small,
                        variant = MaterialUButtonVariant.Filled,
                        enabled = controlsEnabled && uiState.hasPendingChanges,
                    )
                }
            }

            if (createPlaylistOpen) {
                PlaylistCreationBottomSheet(
                    onClose = { createPlaylistOpen = false },
                    onConfirm = { title, description, privacy ->
                        createPlaylistOpen = false
                        addToPlaylistViewModel.createPlaylist(
                            title = title,
                            description = description,
                            privacy = privacy,
                            onStateChanged = onStateChanged,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun AddToPlaylistRow(
    option: AddToPlaylistOption,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        leadingContent = option.thumbnailUrl?.let { url ->
            {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .aspectRatio(1f)
                ) {
                    SquareImage(
                        uri = url,
                        modifier = Modifier.matchParentSize(),
                        cornerRadius = 4.dp,
                    )
                }
            }
        },
        trailingContent = {
            Checkbox(
                checked = checked,
                onCheckedChange = { onToggle() },
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        content = {
            Column {
                Text(
                    text = option.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                option.subtitle?.let { subtitle ->
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
    )
}

@Composable
private fun AddToPlaylistCreateRow(
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        leadingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.PlaylistAdd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        content = {
            Text(
                text = stringResource(R.string.create_playlist),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        },
    )
}
