package ca.ilianokokoro.umihi.music.ui.screens.player.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeDown
import androidx.compose.material.icons.automirrored.outlined.VolumeMute
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var sliderValue by remember(currentVolume) {
        mutableFloatStateOf(currentVolume.toFloat())
    }

    val isBoosted = sliderValue > Constants.Player.Volume.BOOST_THRESHOLD

    fun triggerHaptic() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            }
        } catch (_: Exception) {}
    }

    val volumeIcon: ImageVector = when {
        sliderValue <= 0f -> Icons.AutoMirrored.Outlined.VolumeMute
        sliderValue <= 30f -> Icons.AutoMirrored.Outlined.VolumeDown
        isBoosted -> Icons.Filled.Bolt
        else -> Icons.AutoMirrored.Outlined.VolumeUp
    }

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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = volumeIcon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.in_app_volume),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Volume Percent Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AnimatedVisibility(
                        visible = isBoosted,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(
                                text = "⚡ ${stringResource(R.string.volume_boost_badge)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = "${sliderValue.roundToInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Slider
            Slider(
                value = sliderValue,
                onValueChange = { newValue ->
                    val rounded = newValue.roundToInt()
                    if (rounded == 0 || rounded == 50 || rounded == 100 || rounded == 150 || rounded == 200) {
                        if (sliderValue.roundToInt() != rounded) {
                            triggerHaptic()
                        }
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
                val presets = listOf(
                    0 to "0%",
                    50 to "50%",
                    100 to "100%",
                    150 to "⚡ 150%",
                    200 to "⚡ 200%"
                )

                presets.forEach { (percent, label) ->
                    val isSelected = sliderValue.roundToInt() == percent
                    val isBoostButton = percent > 100

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
                                triggerHaptic()
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
