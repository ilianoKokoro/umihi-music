package ca.ilianokokoro.umihi.music.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongOptionBottomSheet(
    changeVisibility: (visible: Boolean) -> Unit,
    onAddNext: () -> Unit,
    onAddToQueue: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = {
            changeVisibility(false)
        },
        sheetState = rememberModalBottomSheetState(),
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            BottomSheetItem(
                text = stringResource(R.string.play_next),
                icon = Icons.Rounded.PlayCircleOutline,
                onClick = onAddNext
            )
            BottomSheetItem(
                text = stringResource(R.string.add_to_queue),
                icon = Icons.AutoMirrored.Rounded.PlaylistPlay,
                onClick = onAddToQueue
            )
        }
    }
}