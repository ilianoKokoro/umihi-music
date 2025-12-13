package ca.ilianokokoro.umihi.music.ui.components.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository.UpdateChannel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpdateChannelDialog(
    selectedOption: UpdateChannel,
    onChange: (newChannel: UpdateChannel) -> Unit,
    onClose: () -> Unit
) {


    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text(
                text = stringResource(R.string.update_channel_dialog),
            )
        },
        text = {
            val radioOptions = UpdateChannel.entries
            Column(Modifier.selectableGroup()) {
                radioOptions.forEach { option ->
                    val isSelected = option == selectedOption
                    val text = option.toString()
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(shape = RoundedCornerShape(16.dp))
                            .selectable(
                                selected = isSelected,
                                onClick = {
                                    onChange(option)
                                    onClose()
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onClose,
                shapes = ButtonDefaults.shapes()
            )
            {
                Text(stringResource(R.string.close))
            }

        },
        properties = DialogProperties(dismissOnClickOutside = false)
    )


}
