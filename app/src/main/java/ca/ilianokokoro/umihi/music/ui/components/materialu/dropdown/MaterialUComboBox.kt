package ca.ilianokokoro.umihi.music.ui.components.materialu.dropdown

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.ui.components.materialu.MaterialUInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialUComboBox(
    modifier: Modifier = Modifier,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onDismissRequest: () -> Unit = { onExpandedChange(false) },
    label: String? = null,
    leadingIcon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        ) {
            MaterialUInput(
                value = value,
                onValueChange = {},
                readOnly = true,
                label = label,
                leadingIcon = leadingIcon,
                trailingIcon = Icons.Rounded.KeyboardArrowDown,
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = { onExpandedChange(!expanded) })
            )
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            shape = RoundedCornerShape(24.dp),
        ) {
            content()
        }
    }
}