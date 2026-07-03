package ca.ilianokokoro.umihi.music.ui.screens.player.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerBottomSheet(
    changeVisibility: (visible: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    activeRemainingSeconds: Long?,
    onStartTimer: (minutes: Int) -> Unit,
    onStartEndOfSong: () -> Unit,
    onCancelTimer: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = { changeVisibility(false) },
        sheetState = rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            enabledValues = setOf(
                SheetValue.Hidden, SheetValue.Expanded
            )
        ),
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (activeRemainingSeconds != null) {
                val minutes = activeRemainingSeconds / 60
                val seconds = activeRemainingSeconds % 60

                Text(
                    text = stringResource(R.string.sleep_timer_active),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "%d:%02d".format(minutes, seconds),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = stringResource(R.string.sleep_timer_remaining),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }

                Button(
                    onClick = {
                        onCancelTimer()
                        changeVisibility(false)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.cancel_timer))
                }
            } else {
                Text(
                    text = stringResource(R.string.sleep_timer),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )


                FilledTonalButton(
                    onClick = {
                        onStartEndOfSong()
                        changeVisibility(false)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.sleep_timer_end_of_song_btn))
                }


                var sliderValue by remember { mutableIntStateOf(Constants.Ui.Player.SleepTimer.DEFAULT_VALUE) }

                Text(
                    text = stringResource(R.string.minutes, sliderValue),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )


                val range =
                    Constants.Ui.Player.SleepTimer.STEP_VALUE.toFloat()..(Constants.Ui.Player.SleepTimer.STEP_VALUE * Constants.Ui.Player.SleepTimer.STEP_AMOUNT).toFloat()

                Slider(
                    value = sliderValue.toFloat(),
                    onValueChange = { newValue ->
                        val rounded = newValue.roundToInt()
                        if (rounded != sliderValue) {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                        }
                        sliderValue = rounded
                    },
                    valueRange = range,
                    steps = Constants.Ui.Player.SleepTimer.STEP_AMOUNT - 2,
                    modifier = Modifier.fillMaxWidth(),
                )

                val minLabel = Constants.Ui.Player.SleepTimer.STEP_VALUE
                val maxLabel =
                    Constants.Ui.Player.SleepTimer.STEP_VALUE * Constants.Ui.Player.SleepTimer.STEP_AMOUNT

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.short_minutes, minLabel),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.short_minutes, maxLabel),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Button(
                    onClick = {
                        onStartTimer(sliderValue)
                        changeVisibility(false)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.sleep_timer_start))
                }
            }
        }
    }
}
