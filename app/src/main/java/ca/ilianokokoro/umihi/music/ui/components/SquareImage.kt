package ca.ilianokokoro.umihi.music.ui.components

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
fun SquareImage(uri: String, modifier: Modifier = Modifier, cornerRadius: Dp = 12.dp) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(uri)
            .crossfade(Constants.Animation.IMAGE_FADE_DURATION).build(),
        contentDescription = stringResource(R.string.playlist_cover),
        contentScale = ContentScale.Crop,
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(cornerRadius)),
    )
}