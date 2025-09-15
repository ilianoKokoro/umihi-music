package ca.ilianokokoro.umihi.music.ui.screens.player.components.material

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediumIconButton(
    icon: ImageVector, tint: Color = Color.Unspecified,
    background: Color = Color.Unspecified, onClick: () -> Unit
) {
    val size = ButtonDefaults.MediumContainerHeight
    SizedIconButton(
        size = size,
        tint = tint,
        background = background,
        icon = icon,
        onClick = onClick
    )
}
