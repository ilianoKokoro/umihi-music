package ca.ilianokokoro.umihi.music.ui.icons


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
val playlist_play: ImageVector
    get() {
        if (_playlist_play != null) {
            return _playlist_play!!
        }
        _playlist_play =
            ImageVector.Builder(
                name = "playlist_play",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
                autoMirror = true,
            )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.NonZero,
                    ) {
                        moveTo(4f, 16f)
                        quadTo(3.58f, 16f, 3.29f, 15.71f)
                        reflectiveQuadTo(3f, 15f)
                        reflectiveQuadTo(3.29f, 14.29f)
                        reflectiveQuadTo(4f, 14f)
                        horizontalLineToRelative(6f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(11f, 15f)
                        reflectiveQuadToRelative(-0.29f, 0.71f)
                        reflectiveQuadTo(10f, 16f)
                        horizontalLineTo(4f)
                        close()
                        moveTo(4f, 12f)
                        quadTo(3.58f, 12f, 3.29f, 11.71f)
                        quadTo(3f, 11.43f, 3f, 11f)
                        reflectiveQuadTo(3.29f, 10.29f)
                        reflectiveQuadTo(4f, 10f)
                        horizontalLineTo(14f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(15f, 11f)
                        reflectiveQuadToRelative(-0.29f, 0.71f)
                        reflectiveQuadTo(14f, 12f)
                        horizontalLineTo(4f)
                        close()
                        moveTo(4f, 8f)
                        quadTo(3.58f, 8f, 3.29f, 7.71f)
                        quadTo(3f, 7.43f, 3f, 7f)
                        reflectiveQuadTo(3.29f, 6.29f)
                        reflectiveQuadTo(4f, 6f)
                        horizontalLineTo(14f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(15f, 7f)
                        reflectiveQuadTo(14.71f, 7.71f)
                        reflectiveQuadTo(14f, 8f)
                        horizontalLineTo(4f)
                        close()
                        moveTo(16.78f, 20.48f)
                        quadToRelative(-0.13f, 0.07f, -0.25f, 0.07f)
                        reflectiveQuadTo(16.28f, 20.5f)
                        reflectiveQuadToRelative(-0.2f, -0.16f)
                        quadTo(16f, 20.23f, 16f, 20.08f)
                        verticalLineTo(13.93f)
                        quadToRelative(0f, -0.15f, 0.07f, -0.26f)
                        reflectiveQuadToRelative(0.2f, -0.16f)
                        reflectiveQuadToRelative(0.25f, -0.05f)
                        reflectiveQuadToRelative(0.25f, 0.07f)
                        lineToRelative(4.6f, 3.05f)
                        quadToRelative(0.13f, 0.07f, 0.18f, 0.19f)
                        reflectiveQuadTo(21.6f, 17f)
                        reflectiveQuadToRelative(-0.05f, 0.24f)
                        reflectiveQuadToRelative(-0.18f, 0.19f)
                        lineToRelative(-4.6f, 3.05f)
                        close()
                    }
                }
                .build()
        return _playlist_play!!
    }

private var _playlist_play: ImageVector? = null
