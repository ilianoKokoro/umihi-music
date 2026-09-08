package ca.ilianokokoro.umihi.music.ui.components.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.DiagnosticLog
import ca.ilianokokoro.umihi.music.ui.components.SheetHeader
import ca.ilianokokoro.umihi.music.ui.components.materialu.MaterialUButton
import ca.ilianokokoro.umihi.music.ui.components.materialu.MaterialUButtonSize
import ca.ilianokokoro.umihi.music.ui.components.materialu.MaterialUButtonVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsLogBottomSheet(
    onExport: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var logContent by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        logContent = withContext(Dispatchers.IO) {
            DiagnosticLog.getLastLines()
        }
    }
    val scope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Expanded,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SheetHeader(
                icon = Icons.AutoMirrored.Outlined.TextSnippet,
                title = stringResource(R.string.show_logs),
            )

            Text(
                text = logContent,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            ) {
                MaterialUButton(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    text = stringResource(R.string.close),
                    size = MaterialUButtonSize.Small,
                    variant = MaterialUButtonVariant.Tonal,
                )
                MaterialUButton(
                    onClick = {
                        scope.launch {
                            onClear()
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    text = stringResource(R.string.clear_logs),
                    icon = Icons.Outlined.Delete,
                    size = MaterialUButtonSize.Small,
                    variant = MaterialUButtonVariant.Outlined,
                )
                MaterialUButton(
                    onClick = onExport,
                    text = stringResource(R.string.export_logs),
                    icon = Icons.Outlined.Share,
                    size = MaterialUButtonSize.Small,
                    variant = MaterialUButtonVariant.Filled,
                )
            }
        }
    }
}