package ca.ilianokokoro.umihi.music.ui.components.materialu.dropdown

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.PopupProperties

data class DropdownItem(
    val text: String,
    val icon: ImageVector? = null,
    val onClick: () -> Unit,
    val visible: Boolean = true,
)

@Composable
fun MaterialUDropdown(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<DropdownItem>,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember {
        MutableInteractionSource()
    },
    properties: PopupProperties = PopupProperties(
        focusable = true,
    )
) {

    val visibleItems = items.filter { it.visible }

    if (visibleItems.isEmpty()) {
        return
    }


    DropdownMenuPopup(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        properties = properties
    ) {
        DropdownMenuGroup(
            shapes = MenuDefaults.groupShape(0, 1),
            containerColor = MenuDefaults.containerColor,
            interactionSource = interactionSource,
        ) {
            visibleItems.forEachIndexed { index, item ->
                val itemInteractionSource = remember { MutableInteractionSource() }
                MaterialUDropdownItem(
                    interactionSource = itemInteractionSource,
                    item = item,
                    index = index,
                    count = visibleItems.size,
                    onDismissRequest = onDismissRequest,
                )
            }
        }
    }
}

@Composable
fun MaterialUDropdownItem(
    item: DropdownItem,
    index: Int,
    count: Int,
    onDismissRequest: () -> Unit,
    interactionSource: MutableInteractionSource? = null,
) {
    DropdownMenuItem(
        text = {
            Text(
                text = item.text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
            )
        },
        onClick = {
            item.onClick()
            onDismissRequest()
        },
        shape = when {
            count == 1 -> MenuDefaults.standaloneItemShape
            index == 0 -> MenuDefaults.leadingItemShape
            index == count - 1 -> MenuDefaults.trailingItemShape
            else -> MenuDefaults.middleItemShape
        },
        interactionSource = interactionSource,
        leadingIcon = item.icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                )
            }
        },
    )
}
