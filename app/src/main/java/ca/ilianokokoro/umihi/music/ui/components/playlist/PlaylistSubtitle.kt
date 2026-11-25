package ca.ilianokokoro.umihi.music.ui.components.playlist

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.models.PlaylistDownloadState
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.ui.icons.download_for_offline
import ca.ilianokokoro.umihi.music.ui.icons.downloading

private const val DOWNLOADED_ID = "downloaded"
private const val PARTIALLY_DOWNLOADED_ID = "partially-downloaded"
private const val ICON_SIZE = 14

private val playlistInlineContent = mapOf(
    DOWNLOADED_ID to InlineTextContent(
        Placeholder(
            width = ICON_SIZE.sp,
            height = ICON_SIZE.sp,
            placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
        )
    ) {
        Icon(
            imageVector = download_for_offline,
            contentDescription = null,
            modifier = Modifier.size(ICON_SIZE.dp),
        )
    },
    PARTIALLY_DOWNLOADED_ID to InlineTextContent(
        Placeholder(
            width = ICON_SIZE.sp,
            height = ICON_SIZE.sp,
            placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
        )
    ) {
        Icon(
            imageVector = downloading,
            contentDescription = null,
            modifier = Modifier.size(ICON_SIZE.dp),
        )
    },
)

@Composable
fun PlaylistSubtitle(
    playlist: PlaylistInfo,
    modifier: Modifier = Modifier,
) {
    val text = playlist.songCount?.let {
        stringResource(R.string.songs, it)
    }.orEmpty()

    val downloadState = playlist.downloadState

    val label = remember(text, downloadState) {
        buildAnnotatedString {
            append(text)
            when (downloadState) {
                PlaylistDownloadState.NOT_DOWNLOADED -> {}
                PlaylistDownloadState.PARTIALLY_DOWNLOADED -> {
                    if (text.isNotEmpty()) {
                        append(" ")
                    }
                    appendInlineContent(PARTIALLY_DOWNLOADED_ID)
                }

                PlaylistDownloadState.DOWNLOADED -> {
                    if (text.isNotEmpty()) {
                        append(" ")
                    }
                    appendInlineContent(DOWNLOADED_ID)
                }
            }
        }
    }

    Text(
        text = label,
        inlineContent = playlistInlineContent,
        modifier = modifier.padding(top = 2.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}