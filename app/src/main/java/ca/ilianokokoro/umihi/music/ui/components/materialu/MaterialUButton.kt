package ca.ilianokokoro.umihi.music.ui.components.materialu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class MaterialUButtonSize(
    val height: Dp,
    val horizontalPadding: Dp,
    val iconSize: Dp,
    val textScale: Float
) {
    ExtraSmall(32.dp, 12.dp, 16.dp, 0.85f),
    Small(40.dp, 16.dp, 18.dp, 0.95f),
    Medium(56.dp, 20.dp, 22.dp, 1f),
    Large(72.dp, 28.dp, 26.dp, 1.15f),
    ExtraLarge(112.dp, 40.dp, 32.dp, 1.35f)
}

enum class MaterialUButtonVariant {
    Filled,
    Tonal,
    Elevated,
    Outlined,
    Text
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MaterialUButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    enabled: Boolean = true,
    size: MaterialUButtonSize = MaterialUButtonSize.Medium,
    variant: MaterialUButtonVariant = MaterialUButtonVariant.Filled
) {
    val hasText = !text.isNullOrBlank()
    val iconOnly = icon != null && !hasText

    val buttonModifier = modifier
        .minimumInteractiveComponentSize()
        .defaultMinSize(minHeight = size.height)
        .height(size.height)

    val contentPadding = PaddingValues(
        horizontal = if (iconOnly) 0.dp else size.horizontalPadding
    )

    val content: @Composable RowScope.() -> Unit = {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(size.iconSize),
                    tint = LocalContentColor.current
                )
            }

            if (hasText) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = MaterialTheme.typography.labelLarge.fontSize * size.textScale
                    ),
                    maxLines = 1
                )
            }
        }
    }

    when (variant) {
        MaterialUButtonVariant.Filled -> {
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = buttonModifier,
                shapes = ButtonDefaults.shapes(),
                contentPadding = contentPadding,
                colors = ButtonDefaults.buttonColors(),
                content = content
            )
        }

        MaterialUButtonVariant.Tonal -> {
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = buttonModifier,
                shapes = ButtonDefaults.shapes(),
                contentPadding = contentPadding,
                colors = ButtonDefaults.filledTonalButtonColors(),
                content = content
            )
        }

        MaterialUButtonVariant.Elevated -> {
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = buttonModifier,
                shapes = ButtonDefaults.shapes(),
                contentPadding = contentPadding,
                colors = ButtonDefaults.elevatedButtonColors(),
                elevation = ButtonDefaults.elevatedButtonElevation(),
                content = content
            )
        }

        MaterialUButtonVariant.Outlined -> {
            OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                modifier = buttonModifier,
                shapes = ButtonDefaults.shapes(),
                contentPadding = contentPadding,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline
                ),
                content = content
            )
        }

        MaterialUButtonVariant.Text -> {
            TextButton(
                onClick = onClick,
                enabled = enabled,
                modifier = buttonModifier,
                shapes = ButtonDefaults.shapes(),
                contentPadding = contentPadding,
                content = content
            )
        }
    }
}