package ca.ilianokokoro.umihi.music.ui.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.ui.screens.settings.CacheType
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CacheSizeInputDialog(
    cacheType: CacheType,
    initialSizeMB: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // NOTE: onDismiss is called in onDismissRequest but is redundant since the call site
    // already sets showCacheSizeInputDialog=false in onConfirm. Kept for API compatibility.
    val (titleRes, minVal, maxVal) = when (cacheType) {
        CacheType.AUDIO -> Triple(
            R.string.exoplayer_cache_title,
            Constants.Cache.Audio.MIN_SIZE_MB,
            Constants.Cache.Audio.MAX_SIZE_MB
        )

        CacheType.THUMBNAIL -> Triple(
            R.string.thumbnail_cache_title,
            Constants.Cache.Thumbnail.MIN_SIZE_MB,
            Constants.Cache.Thumbnail.MAX_SIZE_MB
        )
    }

    val initialIndex = initialSizeMB.coerceIn(minVal, maxVal)
    var sliderValue by remember { mutableFloatStateOf(initialIndex.toFloat()) }
    val currentValue = sliderValue.roundToInt().coerceIn(minVal, maxVal)

    ModalBottomSheet(
        onDismissRequest = {
            onConfirm(currentValue)
            onDismiss()
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
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
            )

            val haptic = LocalHapticFeedback.current
            Text(
                text = stringResource(R.string.cache_size_mb, currentValue),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Slider(
                value = sliderValue,
                onValueChange = { newValue ->
                    val snapped = newValue.roundToInt()
                        .coerceIn(minVal, maxVal)
                        .toFloat()
                    if (snapped != sliderValue) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    sliderValue = snapped
                },
                valueRange = minVal.toFloat()..maxVal.toFloat(),
                steps = (maxVal - minVal) - 1,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.cache_size_mb, minVal),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.cache_size_mb, maxVal),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
