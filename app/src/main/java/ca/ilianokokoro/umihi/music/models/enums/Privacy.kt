package ca.ilianokokoro.umihi.music.models.enums

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.ui.icons.link
import ca.ilianokokoro.umihi.music.ui.icons.lock
import ca.ilianokokoro.umihi.music.ui.icons.public

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
        icon = public
    ),
    UNLISTED(
        value = "UNLISTED",
        labelRes = R.string.privacy_unlisted,
        icon = link
    ),
    PRIVATE(
        value = "PRIVATE",
        labelRes = R.string.privacy_private,
        icon = lock
    );
}