package ca.ilianokokoro.umihi.music.ui.components.song

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.components.SquareImage
import ca.ilianokokoro.umihi.music.ui.components.materialu.dropdown.MaterialUDropdown
import ca.ilianokokoro.umihi.music.ui.components.materialu.dropdown.MaterialUDropdownItem

@Composable
fun HomeSongCard(
    song: Song,
    onClicked: () -> Unit,
    playNext: () -> Unit,
    addToQueue: () -> Unit,
    modifier: Modifier = Modifier,
    rank: Int? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .width(150.dp)
            .combinedClickable(
                onClick = onClicked,
                onLongClick = { expanded = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                SquareImage(
                    uri = song.thumbnailPath ?: song.thumbnailHref,
                    contentDescription = song.title,
                    modifier = Modifier.matchParentSize()
                )

                if (rank != null) {
                    val (badgeBg, badgeTextColor) = when (rank) {
                        1 -> Color(0xFFFFD700) to Color.Black
                        2 -> Color(0xFFC0C0C0) to Color.Black
                        3 -> Color(0xFFCD7F32) to Color.White
                        else -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "#$rank",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor
                        )
                    }
                }

                if (song.isVideo) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Videocam,
                            contentDescription = stringResource(R.string.video),
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Text(
                text = song.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .basicMarquee(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
            ) {
                if (song.isExplicit) {
                    ExplicitBadge()
                }

                Text(
                    text = song.artist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.basicMarquee()
                )
            }

            MaterialUDropdown(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                MaterialUDropdownItem(
                    leadingIcon = Icons.Rounded.PlayCircleOutline,
                    text = stringResource(R.string.play_next),
                    onClick = {
                        playNext()
                        expanded = false
                    }
                )
                MaterialUDropdownItem(
                    leadingIcon = Icons.AutoMirrored.Rounded.PlaylistPlay,
                    text = stringResource(R.string.add_to_queue),
                    onClick = {
                        addToQueue()
                        expanded = false
                    }
                )
            }
        }
    }
}
