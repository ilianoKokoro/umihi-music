package ca.ilianokokoro.umihi.music.models

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Public
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import ca.ilianokokoro.umihi.music.R

@Immutable
enum class Privacy(
    val value: String,

    @field:StringRes
    val labelRes: Int,

    val icon: ImageVector
) {
    PUBLIC(
        value = "PUBLIC",
        labelRes = R.string.privacy_public,
        icon = Icons.Rounded.Public
    ),
    UNLISTED(
        value = "UNLISTED",
        labelRes = R.string.privacy_unlisted,
        icon = Icons.Rounded.Link
    ),
    PRIVATE(
        value = "PRIVATE",
        labelRes = R.string.privacy_private,
        icon = Icons.Rounded.Lock
    );
}