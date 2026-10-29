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
val text_snippet: ImageVector
    get() {
        if (_text_snippet != null) {
            return _text_snippet!!
        }
        _text_snippet =
            ImageVector.Builder(
                name = "text_snippet",
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
                        moveTo(5f, 21f)
                        quadTo(4.18f, 21f, 3.59f, 20.41f)
                        reflectiveQuadTo(3f, 19f)
                        verticalLineTo(5f)
                        quadTo(3f, 4.17f, 3.59f, 3.59f)
                        reflectiveQuadTo(5f, 3f)
                        horizontalLineToRelative(9.18f)
                        quadToRelative(0.4f, 0f, 0.76f, 0.15f)
                        reflectiveQuadToRelative(0.64f, 0.43f)
                        lineToRelative(4.85f, 4.85f)
                        quadTo(20.7f, 8.7f, 20.85f, 9.06f)
                        quadTo(21f, 9.42f, 21f, 9.82f)
                        verticalLineTo(19f)
                        quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                        reflectiveQuadTo(19f, 21f)
                        horizontalLineTo(5f)
                        close()
                        moveTo(8f, 17f)
                        horizontalLineToRelative(8f)
                        quadToRelative(0.43f, 0f, 0.71f, -0.29f)
                        quadTo(17f, 16.43f, 17f, 16f)
                        reflectiveQuadTo(16.71f, 15.29f)
                        reflectiveQuadTo(16f, 15f)
                        horizontalLineTo(8f)
                        quadTo(7.58f, 15f, 7.29f, 15.29f)
                        reflectiveQuadTo(7f, 16f)
                        reflectiveQuadToRelative(0.29f, 0.71f)
                        reflectiveQuadTo(8f, 17f)
                        close()
                        moveTo(8f, 13f)
                        horizontalLineToRelative(8f)
                        quadToRelative(0.43f, 0f, 0.71f, -0.29f)
                        quadTo(17f, 12.43f, 17f, 12f)
                        reflectiveQuadTo(16.71f, 11.29f)
                        reflectiveQuadTo(16f, 11f)
                        horizontalLineTo(8f)
                        quadTo(7.58f, 11f, 7.29f, 11.29f)
                        reflectiveQuadTo(7f, 12f)
                        reflectiveQuadToRelative(0.29f, 0.71f)
                        reflectiveQuadTo(8f, 13f)
                        close()
                        moveTo(8f, 9f)
                        horizontalLineToRelative(5f)
                        quadToRelative(0.43f, 0f, 0.71f, -0.29f)
                        reflectiveQuadTo(14f, 8f)
                        quadTo(14f, 7.57f, 13.71f, 7.29f)
                        reflectiveQuadTo(13f, 7f)
                        horizontalLineTo(8f)
                        quadTo(7.58f, 7f, 7.29f, 7.29f)
                        reflectiveQuadTo(7f, 8f)
                        quadTo(7f, 8.42f, 7.29f, 8.71f)
                        reflectiveQuadTo(8f, 9f)
                        close()
                    }
                }
                .build()
        return _text_snippet!!
    }

private var _text_snippet: ImageVector? = null
