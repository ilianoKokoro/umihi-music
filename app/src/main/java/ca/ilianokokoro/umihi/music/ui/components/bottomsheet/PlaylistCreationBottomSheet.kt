package ca.ilianokokoro.umihi.music.ui.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.models.Privacy
import ca.ilianokokoro.umihi.music.ui.components.SheetHeader


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistCreationBottomSheet(
    onConfirm: (title: String, description: String, privacy: Privacy) -> Unit,
    onClose: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var privacy by remember { mutableStateOf(Privacy.PRIVATE) }
    var privacyExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onClose,
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
        ) {
            SheetHeader(
                icon = Icons.Rounded.PlaylistAdd,
                title = stringResource(R.string.create_playlist),
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = {
                    Text(stringResource(R.string.title))
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = privacyExpanded,
                onExpandedChange = {
                    privacyExpanded = !privacyExpanded
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = stringResource(privacy.labelRes),
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Text(stringResource(R.string.visibility))
                    },

                    leadingIcon = {
                        Icon(
                            imageVector = privacy.icon,
                            contentDescription = null
                        )
                    },

                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = privacyExpanded
                        )
                    },

                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),

                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = privacyExpanded,
                    onDismissRequest = {
                        privacyExpanded = false
                    }
                ) {
                    Privacy.entries.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(stringResource(option.labelRes))
                            },

                            leadingIcon = {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = null
                                )
                            },

                            onClick = {
                                privacy = option
                                privacyExpanded = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    onClick = onClose
                ) {
                    Text(stringResource(R.string.close))
                }
                TextButton(
                    enabled = title.isNotBlank(),
                    onClick = {
                        onConfirm(
                            title.trim(),
                            description.trim(),
                            privacy
                        )
                    }
                ) {
                    Text(stringResource(R.string.create))
                }
            }
        }
    }
}