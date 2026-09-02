package ca.ilianokokoro.umihi.music.error

import android.content.ClipData
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.ui.components.SheetHeader
import ca.ilianokokoro.umihi.music.ui.components.materialu.MaterialUButton
import ca.ilianokokoro.umihi.music.ui.components.materialu.MaterialUButtonSize
import ca.ilianokokoro.umihi.music.ui.components.materialu.MaterialUButtonVariant
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorLogBottomSheet(
    context: Context,
    onDismissRequest: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    fullErrorLog: String,
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Expanded,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SheetHeader(
                icon = Icons.Outlined.BugReport,
                title = dialogTitle,
            )

            Text(
                text = dialogText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MaterialUButton(
                    onClick = {
                        scope.launch {
                            val clip = ClipData.newPlainText(
                                context.getString(R.string.error_log),
                                fullErrorLog
                            )
                            clipboard.setClipEntry(ClipEntry(clip))
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.copied),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    text = stringResource(R.string.copy_logs),
                    variant = MaterialUButtonVariant.Filled,
                    size = MaterialUButtonSize.Small,
                    modifier = Modifier.weight(1f),
                )

                MaterialUButton(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            onDismissRequest()
                        }
                    },
                    text = stringResource(R.string.close),
                    variant = MaterialUButtonVariant.Tonal,
                    size = MaterialUButtonSize.Small,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
