package ca.ilianokokoro.umihi.music.ui.components.materialu.dropdown

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SelectableDropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import ca.ilianokokoro.umihi.music.ui.components.materialu.MaterialUInput
import ca.ilianokokoro.umihi.music.ui.icons.keyboard_arrow_down

data class ComboBoxItem(
    val text: String,
    val icon: ImageVector? = null,
    val onClick: () -> Unit,
    val selected: Boolean = false,
)

@Composable
fun MaterialUComboBox(
    modifier: Modifier = Modifier,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onDismissRequest: () -> Unit = { onExpandedChange(false) },
    label: String? = null,
    leadingIcon: ImageVector? = null,
    items: List<ComboBoxItem>,
) {
    val state = rememberTextFieldState(initialText = value)

    LaunchedEffect(value) {
        if (state.text.toString() != value) {
            state.setTextAndPlaceCursorAtEnd(value)
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier,
    ) {
        MaterialUInput(
            state = state,
            readOnly = true,
            label = label,
            leadingIcon = leadingIcon,
            trailingIcon = keyboard_arrow_down,
            modifier = Modifier.menuAnchor(
                ExposedDropdownMenuAnchorType.PrimaryNotEditable,
            ),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = Modifier.exposedDropdownSize(),
            containerColor = MenuDefaults.groupStandardContainerColor,
            shape = MenuDefaults.standaloneGroupShape,
        ) {

            items.forEachIndexed { index, item ->
                MaterialUComboBoxItem(
                    item = item,
                    index = index,
                    count = items.size,
                    onDismissRequest = onDismissRequest,
                )
            }
        }

    }
}

@Composable
private fun MaterialUComboBoxItem(
    item: ComboBoxItem,
    index: Int,
    count: Int,
    onDismissRequest: () -> Unit,
) {
    SelectableDropdownMenuItem(
        selected = item.selected,

        text = { Text(item.text, style = MaterialTheme.typography.bodyLarge) },
        shapes = MenuDefaults.itemShape(index, count),
        onClick = {
            item.onClick()
            onDismissRequest()
        },
        leadingIcon = item.icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                )
            }
        },
        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
    )
}