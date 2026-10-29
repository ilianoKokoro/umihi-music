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
val image: ImageVector
    get() {
        if (_image != null) {
            return _image!!
        }
        _image =
            ImageVector.Builder(
                name = "image",
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
                        moveTo(5f, 21f)
                        quadTo(4.18f, 21f, 3.59f, 20.41f)
                        reflectiveQuadTo(3f, 19f)
                        verticalLineTo(5f)
                        quadTo(3f, 4.17f, 3.59f, 3.59f)
                        reflectiveQuadTo(5f, 3f)
                        horizontalLineTo(19f)
                        quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                        reflectiveQuadTo(21f, 5f)
                        verticalLineTo(19f)
                        quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                        reflectiveQuadTo(19f, 21f)
                        horizontalLineTo(5f)
                        close()
                        moveTo(7f, 17f)
                        horizontalLineTo(17f)
                        quadToRelative(0.3f, 0f, 0.45f, -0.27f)
                        reflectiveQuadTo(17.4f, 16.2f)
                        lineTo(14.65f, 12.52f)
                        quadToRelative(-0.15f, -0.2f, -0.4f, -0.2f)
                        reflectiveQuadToRelative(-0.4f, 0.2f)
                        lineTo(11.25f, 16f)
                        lineTo(9.4f, 13.52f)
                        quadTo(9.25f, 13.33f, 9f, 13.33f)
                        reflectiveQuadToRelative(-0.4f, 0.2f)
                        lineToRelative(-2f, 2.68f)
                        quadTo(6.4f, 16.45f, 6.55f, 16.73f)
                        reflectiveQuadTo(7f, 17f)
                        close()
                    }
                }
                .build()
        return _image!!
    }

private var _image: ImageVector? = null
