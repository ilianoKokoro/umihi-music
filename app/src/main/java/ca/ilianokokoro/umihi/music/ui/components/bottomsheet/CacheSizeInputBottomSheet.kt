package ca.ilianokokoro.umihi.music.ui.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import ca.ilianokokoro.umihi.music.ui.screens.settings.CacheType
import ca.ilianokokoro.umihi.music.ui.components.SheetHeader
import kotlin.math.roundToInt

private data class CacheSizeConfig(
    val titleRes: Int,
    val minVal: Int,
    val maxVal: Int,
    val step: Int,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CacheSizeInputBottomSheet(
    cacheType: CacheType,
    initialSizeMB: Int,
    onConfirm: (Int) -> Unit,
) {
    val config = when (cacheType) {
        CacheType.AUDIO -> CacheSizeConfig(
            R.string.exoplayer_cache_title,
            Constants.Cache.Audio.MIN_SIZE_MB,
            Constants.Cache.Audio.MAX_SIZE_MB,
            Constants.Cache.Audio.STEP_MB
        )

        CacheType.THUMBNAIL -> CacheSizeConfig(
            R.string.thumbnail_cache_title,
            Constants.Cache.Thumbnail.MIN_SIZE_MB,
            Constants.Cache.Thumbnail.MAX_SIZE_MB,
            Constants.Cache.Thumbnail.STEP_MB
        )
    }

    val initialIndex = initialSizeMB.coerceIn(config.minVal, config.maxVal)
    var sliderValue by remember { mutableFloatStateOf(initialIndex.toFloat()) }
    val currentValue = ((sliderValue - config.minVal) / config.step).roundToInt()
        .coerceIn(0, (config.maxVal - config.minVal) / config.step) * config.step + config.minVal

    ModalBottomSheet(
        onDismissRequest = {
            onConfirm(currentValue)
        },
        sheetState = rememberBottomSheetState(
            initialValue = SheetValue.Expanded,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SheetHeader(
                    icon = Icons.Outlined.Storage,
                    title = stringResource(config.titleRes),
                )
                Text(
                    text = stringResource(R.string.cache_size_mb, currentValue),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            val haptic = LocalHapticFeedback.current

            Slider(
                value = sliderValue,
                onValueChange = { newValue ->
                    val snapped = newValue.roundToInt()
                        .coerceIn(config.minVal, config.maxVal)
                        .toFloat()
                    if (snapped != sliderValue) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    sliderValue = snapped
                },
                valueRange = config.minVal.toFloat()..config.maxVal.toFloat(),
                steps = ((config.maxVal - config.minVal) / config.step) - 1,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.cache_size_mb, config.minVal),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.cache_size_mb, config.maxVal),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
