package ca.ilianokokoro.umihi.music.ui.components.playlist

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ca.ilianokokoro.umihi.music.R
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
    val screenState = sharedTransitionScope.rememberSharedContentState("playlist_screen_${playlistInfo.id}")
    val coverState = sharedTransitionScope.rememberSharedContentState("playlist_cover_${playlistInfo.id}")
    val titleState = sharedTransitionScope.rememberSharedContentState("playlist_title_${playlistInfo.id}")
    val countState = sharedTransitionScope.rememberSharedContentState("playlist_count_${playlistInfo.id}")

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
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .sharedBounds(
                            sharedContentState = titleState,
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = playlistInfo.songCount?.let { songCount ->
                    stringResource(R.string.songs, songCount)
                }.orEmpty(),
                modifier = with(sharedTransitionScope) {
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                        .sharedBounds(
                            sharedContentState = countState,
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
