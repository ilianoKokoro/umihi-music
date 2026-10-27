package ca.ilianokokoro.umihi.music.ui.components.song

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material3.Icon
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
import ca.ilianokokoro.umihi.music.models.Song


private const val DOWNLOADED_ID = "downloaded"
private const val EXPLICIT_ID = "explicit"

private const val DOWNLOADED_SIZE = 16
private const val EXPLICIT_SIZE = 20

private val songInlineContent = mapOf(
    DOWNLOADED_ID to InlineTextContent(
        Placeholder(
            width = DOWNLOADED_SIZE.sp,
            height = DOWNLOADED_SIZE.sp,
            placeholderVerticalAlign = PlaceholderVerticalAlign.Center
        )
    ) {
        Icon(
            imageVector = Icons.Rounded.DownloadForOffline,
            contentDescription = null,
            modifier = Modifier.size(DOWNLOADED_SIZE.dp)
        )
    },
    EXPLICIT_ID to InlineTextContent(
        Placeholder(
            width = EXPLICIT_SIZE.sp,
            height = EXPLICIT_SIZE.sp,
            placeholderVerticalAlign = PlaceholderVerticalAlign.Center
        )
    ) {
        ExplicitBadge(
            size = EXPLICIT_SIZE.dp,
        )
    }
)

@Composable
fun SongSubtitle(song: Song, modifier: Modifier = Modifier) {
    val dot = stringResource(R.string.dot)

    val text = "${song.artists} $dot ${song.duration}"
    val isDownloaded = song.downloaded
    val isExplicit = song.isExplicit
    val label = remember(text, isDownloaded, isExplicit) {
        buildAnnotatedString {
            if (isDownloaded) {
                appendInlineContent(DOWNLOADED_ID)
                append(" ")
            }
            if (isExplicit) {
                appendInlineContent(EXPLICIT_ID)
                append(" ")
            }
            append(text)
        }
    }

    Text(
        text = label,
        inlineContent = songInlineContent,
        modifier = modifier,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}