package ca.ilianokokoro.umihi.music.ui.components.playlist

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.ui.components.SquareImage

@Composable
fun PlaylistCard(
    playlistInfo: PlaylistInfo,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenState = sharedTransitionScope.rememberSharedContentState(
        "${Constants.SharedTransition.PLAYLIST_SCREEN_KEY}${playlistInfo.id}"
    )
    val coverState = sharedTransitionScope.rememberSharedContentState(
        "${Constants.SharedTransition.PLAYLIST_COVER_KEY}${playlistInfo.id}"
    )
    val titleState = sharedTransitionScope.rememberSharedContentState(
        "${Constants.SharedTransition.PLAYLIST_TITLE_KEY}${playlistInfo.id}"
    )
    val countState = sharedTransitionScope.rememberSharedContentState(
        "${Constants.SharedTransition.PLAYLIST_COUNT_KEY}${playlistInfo.id}"
    )

    Card(
        onClick = onClicked,
        modifier = with(sharedTransitionScope) {
            modifier
                .fillMaxSize()
                .sharedBounds(
                    sharedContentState = screenState,
                    animatedVisibilityScope = animatedVisibilityScope,
                    zIndexInOverlay = -1f,
                )
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize()
        ) {
            if (!playlistInfo.isDownloadedPlaylist) {
                SquareImage(
                    uri = playlistInfo.coverPath ?: playlistInfo.coverHref,
                    contentDescription = stringResource(R.string.playlist_cover),
                    modifier = with(sharedTransitionScope) {
                        Modifier.sharedElement(
                            sharedContentState = coverState,
                            animatedVisibilityScope = animatedVisibilityScope,
                            clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(12.dp)),
                        )
                    }
                )
            } else {
                OfflineThumbnail(
                    modifier = with(sharedTransitionScope) {
                        Modifier.sharedElement(
                            sharedContentState = coverState,
                            animatedVisibilityScope = animatedVisibilityScope,
                            clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(12.dp)),
                        )
                    }
                )
            }

            Text(
                text = playlistInfo.title,
                modifier = with(sharedTransitionScope) {
                    Modifier
                        .padding(top = 8.dp)
                        .sharedBounds(
                            sharedContentState = titleState,
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = playlistInfo.songCount?.let { songCount ->
                    stringResource(R.string.songs, songCount)
                }.orEmpty(),
                modifier = with(sharedTransitionScope) {
                    Modifier
                        .padding(top = 2.dp)
                        .sharedBounds(
                            sharedContentState = countState,
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
