package ca.ilianokokoro.umihi.music.ui.screens.player.components.material

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SizedIconButton(
    size: Dp,
    icon: ImageVector,
    tint: Color = Color.Unspecified,
    background: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.heightIn(size),
        contentPadding = ButtonDefaults.contentPaddingFor(size),
        shapes = ButtonDefaults.shapes(),
        colors = ButtonDefaults.buttonColors(containerColor = background)
    ) {
        Icon(
            tint = tint,
            imageVector = icon,
            contentDescription = icon.toString(),
            modifier = Modifier.size(ButtonDefaults.iconSizeFor(size)),
        )
    }
}
