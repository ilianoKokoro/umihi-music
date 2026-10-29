package ca.ilianokokoro.umihi.music.ui.components

import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.ui.icons.arrow_back

@Composable
fun BackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledIconButton(
        onClick = onBack,
        modifier = modifier,
        shapes = IconButtonDefaults.shapes(),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
    ) {
        Icon(
            imageVector = arrow_back,
            contentDescription = stringResource(R.string.back_description),
        )
    }
}
