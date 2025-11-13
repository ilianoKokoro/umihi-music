@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package ca.ilianokokoro.umihi.music.error

import android.content.ClipData
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import ca.ilianokokoro.umihi.music.R
import kotlinx.coroutines.launch

@Composable
fun ErrorLogDialog(
    context: Context,
    onDismissRequest: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    fullErrorLog: String
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { onDismissRequest() },
        title = { Text(text = dialogTitle) },
        text = { Text(text = dialogText) },
        confirmButton = {},
        dismissButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
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
                    shapes = ButtonDefaults.shapes(),

                    ) {
                    Text(stringResource(R.string.copy_logs))
                }

                TextButton(onClick = {
                    onDismissRequest()
                }, shapes = ButtonDefaults.shapes()) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    )
}