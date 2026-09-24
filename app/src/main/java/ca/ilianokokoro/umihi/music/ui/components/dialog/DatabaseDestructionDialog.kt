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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.events.DatabaseEvents

@Composable
fun DatabaseDestructionDialog(
) {
    val showMigrationDialog by DatabaseEvents.destructiveMigrationOccurred.collectAsStateWithLifecycle()

    if (showMigrationDialog) {
        AlertDialog(
            onDismissRequest = { DatabaseEvents.consume() },
            title = {
                Text(
                    text = stringResource(R.string.database_desctruction_title)
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = stringResource(R.string.database_desctruction_text),
                        style = MaterialTheme.typography.bodySmall

                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { DatabaseEvents.consume() },
                    shapes = ButtonDefaults.shapes()
                ) { Text(stringResource(R.string.ok)) }

            },
            properties = DialogProperties(dismissOnClickOutside = false)
        )
    }
}

