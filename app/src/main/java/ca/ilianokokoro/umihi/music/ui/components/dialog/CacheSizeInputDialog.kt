package ca.ilianokokoro.umihi.music.ui.components.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.ui.screens.settings.CacheType

@Composable
fun CacheSizeInputDialog(
    cacheType: CacheType,
    initialSizeMB: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var textValue by remember { mutableStateOf(initialSizeMB.toString()) }
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

    val currentInt = textValue.toIntOrNull()
    val isValid = currentInt != null && currentInt in minVal..maxVal

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(titleRes))
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.enter_cache_size_mb))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { newText ->
                        if (newText.all { it.isDigit() } && newText.length <= 5) {
                            textValue = newText
                        }
                    },
                    label = { Text("MB ($minVal - $maxVal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = textValue.isNotEmpty() && !isValid,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    currentInt?.let { onConfirm(it) }
                },
                enabled = isValid,
                shapes = ButtonDefaults.shapes()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shapes = ButtonDefaults.shapes()
            ) {
                Text(stringResource(R.string.close))
            }
        },
        properties = DialogProperties(dismissOnClickOutside = true)
    )
}
