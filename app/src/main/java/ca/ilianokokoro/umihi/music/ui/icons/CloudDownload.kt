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
val cloud_download: ImageVector
    get() {
        if (_cloud_download != null) {
            return _cloud_download!!
        }
        _cloud_download =
            ImageVector.Builder(
                name = "cloud_download",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
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
                        moveTo(6.5f, 20f)
                        quadTo(4.23f, 20f, 2.61f, 18.43f)
                        reflectiveQuadTo(1f, 14.58f)
                        quadTo(1f, 12.63f, 2.18f, 11.1f)
                        reflectiveQuadTo(5.25f, 9.15f)
                        quadTo(5.83f, 7.13f, 7.39f, 5.75f)
                        reflectiveQuadTo(11f, 4.07f)
                        verticalLineToRelative(8.07f)
                        lineTo(10.1f, 11.27f)
                        quadTo(9.83f, 11f, 9.41f, 11f)
                        reflectiveQuadTo(8.7f, 11.3f)
                        quadTo(8.43f, 11.58f, 8.43f, 12f)
                        reflectiveQuadTo(8.7f, 12.7f)
                        lineToRelative(2.6f, 2.6f)
                        quadToRelative(0.3f, 0.3f, 0.7f, 0.3f)
                        reflectiveQuadToRelative(0.7f, -0.3f)
                        lineToRelative(2.6f, -2.6f)
                        quadToRelative(0.28f, -0.28f, 0.29f, -0.69f)
                        reflectiveQuadTo(15.3f, 11.3f)
                        quadTo(15.03f, 11.02f, 14.61f, 11.01f)
                        reflectiveQuadTo(13.9f, 11.27f)
                        lineTo(13f, 12.15f)
                        verticalLineTo(4.07f)
                        quadToRelative(2.58f, 0.35f, 4.29f, 2.31f)
                        reflectiveQuadTo(19f, 11f)
                        quadToRelative(1.73f, 0.2f, 2.86f, 1.49f)
                        reflectiveQuadTo(23f, 15.5f)
                        quadToRelative(0f, 1.88f, -1.31f, 3.19f)
                        reflectiveQuadTo(18.5f, 20f)
                        horizontalLineTo(6.5f)
                        close()
                    }
                }
                .build()
        return _cloud_download!!
    }

private var _cloud_download: ImageVector? = null
