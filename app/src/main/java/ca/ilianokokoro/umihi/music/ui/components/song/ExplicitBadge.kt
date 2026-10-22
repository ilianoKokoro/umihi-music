package ca.ilianokokoro.umihi.music.ui.components.song

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explicit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R

@Composable
fun ExplicitBadge(size: Dp = 20.dp) {
    Icon(
        imageVector = Icons.Rounded.Explicit,
        contentDescription = stringResource(R.string.explicit),
        modifier = Modifier.size(size),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}