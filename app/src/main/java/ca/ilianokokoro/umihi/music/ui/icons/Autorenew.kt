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
val autorenew: ImageVector
    get() {
        if (_autorenew != null) {
            return _autorenew!!
        }
        _autorenew =
            ImageVector.Builder(
                name = "autorenew",
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
                        moveTo(6f, 12.05f)
                        quadToRelative(0f, 0.4f, 0.05f, 0.79f)
                        quadTo(6.1f, 13.23f, 6.23f, 13.6f)
                        quadTo(6.35f, 14.02f, 6.2f, 14.41f)
                        reflectiveQuadTo(5.68f, 14.98f)
                        quadToRelative(-0.4f, 0.2f, -0.79f, 0.04f)
                        reflectiveQuadTo(4.38f, 14.43f)
                        quadTo(4.18f, 13.85f, 4.09f, 13.25f)
                        reflectiveQuadTo(4f, 12.05f)
                        quadTo(4f, 8.7f, 6.33f, 6.35f)
                        reflectiveQuadTo(12f, 4f)
                        horizontalLineToRelative(0.18f)
                        lineTo(11.28f, 3.1f)
                        quadTo(11f, 2.82f, 11f, 2.4f)
                        reflectiveQuadTo(11.28f, 1.7f)
                        quadToRelative(0.28f, -0.28f, 0.7f, -0.28f)
                        reflectiveQuadToRelative(0.7f, 0.28f)
                        lineToRelative(2.6f, 2.6f)
                        quadToRelative(0.3f, 0.3f, 0.3f, 0.7f)
                        reflectiveQuadToRelative(-0.3f, 0.7f)
                        lineToRelative(-2.6f, 2.6f)
                        quadTo(12.4f, 8.57f, 11.98f, 8.57f)
                        reflectiveQuadTo(11.28f, 8.3f)
                        reflectiveQuadTo(11f, 7.6f)
                        quadTo(11f, 7.18f, 11.28f, 6.9f)
                        lineTo(12.18f, 6f)
                        horizontalLineTo(12f)
                        quadTo(9.5f, 6f, 7.75f, 7.76f)
                        reflectiveQuadTo(6f, 12.05f)
                        close()
                        moveToRelative(12f, -0.1f)
                        quadToRelative(0f, -0.4f, -0.05f, -0.79f)
                        quadTo(17.9f, 10.77f, 17.78f, 10.4f)
                        quadTo(17.65f, 9.98f, 17.8f, 9.59f)
                        reflectiveQuadTo(18.33f, 9.02f)
                        quadToRelative(0.4f, -0.2f, 0.79f, -0.04f)
                        reflectiveQuadToRelative(0.51f, 0.59f)
                        quadToRelative(0.2f, 0.57f, 0.29f, 1.18f)
                        reflectiveQuadTo(20f, 11.95f)
                        quadToRelative(0f, 3.35f, -2.32f, 5.7f)
                        reflectiveQuadTo(12f, 20f)
                        horizontalLineTo(11.83f)
                        lineToRelative(0.9f, 0.9f)
                        quadTo(13f, 21.18f, 13f, 21.6f)
                        reflectiveQuadToRelative(-0.27f, 0.7f)
                        reflectiveQuadToRelative(-0.7f, 0.27f)
                        reflectiveQuadTo(11.33f, 22.3f)
                        lineTo(8.73f, 19.7f)
                        quadTo(8.43f, 19.4f, 8.43f, 19f)
                        reflectiveQuadToRelative(0.3f, -0.7f)
                        lineToRelative(2.6f, -2.6f)
                        quadToRelative(0.28f, -0.28f, 0.7f, -0.28f)
                        reflectiveQuadToRelative(0.7f, 0.28f)
                        reflectiveQuadTo(13f, 16.4f)
                        quadToRelative(0f, 0.43f, -0.27f, 0.7f)
                        lineTo(11.83f, 18f)
                        horizontalLineTo(12f)
                        quadToRelative(2.5f, 0f, 4.25f, -1.76f)
                        reflectiveQuadTo(18f, 11.95f)
                        close()
                    }
                }
                .build()
        return _autorenew!!
    }

private var _autorenew: ImageVector? = null
