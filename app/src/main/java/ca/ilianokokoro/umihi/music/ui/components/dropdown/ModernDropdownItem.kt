package ca.ilianokokoro.umihi.music.ui.components.dropdown

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ModernDropdownItem(
    text: String,
    onClick: () -> Unit,
    leadingIcon: ImageVector,
) {
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(12.dp)),
        leadingIcon = { Icon(imageVector = leadingIcon, contentDescription = text) },
    )
}