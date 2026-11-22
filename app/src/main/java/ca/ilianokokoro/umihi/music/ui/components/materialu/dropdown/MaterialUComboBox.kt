package ca.ilianokokoro.umihi.music.ui.components.materialu.dropdown

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.ui.components.materialu.MaterialUInput
import ca.ilianokokoro.umihi.music.ui.icons.keyboard_arrow_down

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
        Box(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        ) {
            MaterialUInput(
                state = state,
                readOnly = true,
                label = label,
                leadingIcon = leadingIcon,
                trailingIcon = keyboard_arrow_down,
            )
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            shape = RoundedCornerShape(24.dp),
            content = content,
        )
    }
}
