package ca.ilianokokoro.umihi.music.ui.components.bottomsheet

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.ui.components.SheetHeader
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolumeBottomSheet(
    changeVisibility: (Boolean) -> Unit,
    currentVolume: Int,
    onVolumeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState =
        rememberBottomSheetState(
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
            initialValue = SheetValue.Hidden
        )
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var sliderValue by remember {
        mutableFloatStateOf(currentVolume.toFloat())
    }

    val isBoosted = sliderValue > Constants.Player.Volume.BOOST_THRESHOLD

    val accentColor by animateColorAsState(
        targetValue = if (isBoosted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        label = "VolumeAccentColor"
    )

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                changeVisibility(false)
            }
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SheetHeader(
                    icon = Icons.AutoMirrored.Rounded.VolumeUp,
                    title = stringResource(R.string.in_app_volume),
                    tint = accentColor,
                )

                Text(
                    text = "${sliderValue.roundToInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )

            }

            Spacer(modifier = Modifier.height(20.dp))

            // Slider
            Slider(
                value = sliderValue,
                onValueChange = { newValue ->
                    val rounded = newValue.roundToInt()
                    if (Constants.Player.Volume.PRESETS.contains(rounded) && sliderValue.roundToInt() != rounded) {
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                    }
                    sliderValue = newValue
                    onVolumeChange(rounded)
                },
                valueRange = Constants.Player.Volume.MIN_PERCENT.toFloat()..Constants.Player.Volume.MAX_PERCENT.toFloat(),
                steps = 0,
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Preset Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val presets = Constants.Player.Volume.PRESETS.map { it to "$it%" }

                presets.forEach { (percent, label) ->
                    val isSelected = sliderValue.roundToInt() == percent
                    val isBoostButton = percent > Constants.Player.Volume.BOOST_THRESHOLD

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            isSelected && isBoostButton -> MaterialTheme.colorScheme.error
                            isSelected -> MaterialTheme.colorScheme.primary
                            isBoostButton -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                                sliderValue = percent.toFloat()
                                onVolumeChange(percent)
                            }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isSelected && isBoostButton -> MaterialTheme.colorScheme.onError
                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                isBoostButton -> MaterialTheme.colorScheme.onErrorContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
