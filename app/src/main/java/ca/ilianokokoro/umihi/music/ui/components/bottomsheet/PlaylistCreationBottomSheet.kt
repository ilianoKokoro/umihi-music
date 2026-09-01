package ca.ilianokokoro.umihi.music.ui.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.models.Privacy
import ca.ilianokokoro.umihi.music.ui.components.SheetHeader
import ca.ilianokokoro.umihi.music.ui.components.materialu.MaterialUButton
import ca.ilianokokoro.umihi.music.ui.components.materialu.MaterialUButtonSize
import ca.ilianokokoro.umihi.music.ui.components.materialu.MaterialUButtonVariant
import ca.ilianokokoro.umihi.music.ui.components.materialu.MaterialUInput
import ca.ilianokokoro.umihi.music.ui.components.materialu.dropdown.MaterialUComboBox
import ca.ilianokokoro.umihi.music.ui.components.materialu.dropdown.MaterialUComboBoxItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistCreationBottomSheet(
    onConfirm: (title: String, description: String, privacy: Privacy) -> Unit,
    onClose: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var privacy by rememberSaveable { mutableStateOf(Privacy.PRIVATE) }
    var privacyExpanded by rememberSaveable { mutableStateOf(false) }

    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    val scope = rememberCoroutineScope()

    fun dismiss() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onClose() }
    }

    ModalBottomSheet(
        onDismissRequest = { dismiss() },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SheetHeader(
                icon = Icons.AutoMirrored.Rounded.PlaylistAdd,
                title = stringResource(R.string.create_playlist),
            )

            MaterialUInput(
                value = title,
                onValueChange = { title = it },
                label = stringResource(R.string.title),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )

            MaterialUInput(
                value = description,
                onValueChange = { description = it },
                label = stringResource(R.string.description),
                singleLine = false,
                minLines = 2,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            )

            MaterialUComboBox(
                value = stringResource(privacy.labelRes),
                expanded = privacyExpanded,
                onExpandedChange = { privacyExpanded = it },
                label = stringResource(R.string.visibility),
                leadingIcon = privacy.icon,
            ) {
                Privacy.entries.forEach { option ->
                    MaterialUComboBoxItem(
                        text = stringResource(option.labelRes),
                        leadingIcon = option.icon,
                        onClick = {
                            privacy = option
                            privacyExpanded = false
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            ) {
                MaterialUButton(
                    onClick = { dismiss() },
                    text = stringResource(R.string.cancel),
                    size = MaterialUButtonSize.Small,
                    variant = MaterialUButtonVariant.Tonal,
                )
                MaterialUButton(
                    onClick = {
                        onConfirm(
                            title.trim(),
                            description.trim(),
                            privacy
                        )
                    },
                    text = stringResource(R.string.create),
                    enabled = title.isNotBlank(),
                    size = MaterialUButtonSize.Small,
                    variant = MaterialUButtonVariant.Filled,
                )
            }
        }
    }
}