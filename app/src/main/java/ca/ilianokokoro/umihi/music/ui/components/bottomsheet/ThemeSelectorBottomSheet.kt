package ca.ilianokokoro.umihi.music.ui.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.models.ThemeMode
import ca.ilianokokoro.umihi.music.ui.components.SheetHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectorBottomSheet(
    selectedOption: ThemeMode,
    onChange: (newTheme: ThemeMode) -> Unit,
    onClose: () -> Unit
) {
    var selected by remember(selectedOption) { mutableStateOf(selectedOption) }

    ModalBottomSheet(
        onDismissRequest = {
            onChange(selected)
            onClose()
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
            SheetHeader(
                icon = Icons.Outlined.Palette,
                title = stringResource(R.string.choose_theme),
                modifier = Modifier.fillMaxWidth(),
            )

            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ThemeMode.entries.forEach { option ->
                    val isSelected = option == selected
                    val (label, icon) = when (option) {
                        ThemeMode.DARK -> stringResource(R.string.theme_dark) to Icons.Outlined.DarkMode
                        ThemeMode.LIGHT -> stringResource(R.string.theme_light) to Icons.Outlined.LightMode
                        ThemeMode.SYSTEM -> stringResource(R.string.theme_system) to Icons.Outlined.BrightnessAuto
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .selectable(
                                selected = isSelected,
                                onClick = {
                                    selected = option
                                    onChange(option)
                                    onClose()
                                },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null,
                        )
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .size(22.dp)
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                }
            }
        }
    }
}
