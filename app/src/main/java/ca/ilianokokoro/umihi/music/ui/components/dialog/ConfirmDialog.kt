package ca.ilianokokoro.umihi.music.ui.components.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import ca.ilianokokoro.umihi.music.R


@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        title = {
            Text(
                text = title
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall

                )

            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                },
                shapes = ButtonDefaults.shapes()
            ) { Text(stringResource(R.string.confirm)) }

        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                },
                shapes = ButtonDefaults.shapes()
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        properties = DialogProperties(dismissOnClickOutside = true)
    )
}



