package ca.ilianokokoro.umihi.music.ui.screens.playlist.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.ui.components.materialu.MaterialUButton
import ca.ilianokokoro.umihi.music.ui.components.materialu.MaterialUButtonSize
import ca.ilianokokoro.umihi.music.ui.components.materialu.MaterialUButtonVariant


@Composable
fun ActionButtons(
    buttonEnabled: Boolean,
    onPlayClicked: () -> Unit,
    onShuffleClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MaterialUButton(
            enabled = buttonEnabled,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                onPlayClicked()
            },
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.PlayArrow,
            text = stringResource(R.string.play),
            size = MaterialUButtonSize.Small,
            variant = MaterialUButtonVariant.Tonal,
        )

        MaterialUButton(
            enabled = buttonEnabled,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                onShuffleClicked()
            },
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Shuffle,
            text = stringResource(R.string.shuffle),
            size = MaterialUButtonSize.Small,
            variant = MaterialUButtonVariant.Tonal,
        )
    }
}