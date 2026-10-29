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
val storage: ImageVector
    get() {
        if (_storage != null) {
            return _storage!!
        }
        _storage =
            ImageVector.Builder(
                name = "storage",
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
                        moveTo(5f, 20f)
                        quadTo(4.18f, 20f, 3.59f, 19.41f)
                        reflectiveQuadTo(3f, 18f)
                        reflectiveQuadTo(3.59f, 16.59f)
                        reflectiveQuadTo(5f, 16f)
                        horizontalLineTo(19f)
                        quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                        quadTo(21f, 17.18f, 21f, 18f)
                        reflectiveQuadToRelative(-0.59f, 1.41f)
                        reflectiveQuadTo(19f, 20f)
                        horizontalLineTo(5f)
                        close()
                        moveTo(5f, 8f)
                        quadTo(4.18f, 8f, 3.59f, 7.41f)
                        reflectiveQuadTo(3f, 6f)
                        reflectiveQuadTo(3.59f, 4.59f)
                        reflectiveQuadTo(5f, 4f)
                        horizontalLineTo(19f)
                        quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                        quadTo(21f, 5.18f, 21f, 6f)
                        reflectiveQuadTo(20.41f, 7.41f)
                        reflectiveQuadTo(19f, 8f)
                        horizontalLineTo(5f)
                        close()
                        moveToRelative(0f, 6f)
                        quadTo(4.18f, 14f, 3.59f, 13.41f)
                        reflectiveQuadTo(3f, 12f)
                        reflectiveQuadTo(3.59f, 10.59f)
                        reflectiveQuadTo(5f, 10f)
                        horizontalLineTo(19f)
                        quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                        quadTo(21f, 11.18f, 21f, 12f)
                        reflectiveQuadToRelative(-0.59f, 1.41f)
                        reflectiveQuadTo(19f, 14f)
                        horizontalLineTo(5f)
                        close()
                        moveTo(6.71f, 6.71f)
                        quadTo(7f, 6.43f, 7f, 6f)
                        reflectiveQuadTo(6.71f, 5.29f)
                        reflectiveQuadTo(6f, 5f)
                        quadTo(5.58f, 5f, 5.29f, 5.29f)
                        reflectiveQuadTo(5f, 6f)
                        reflectiveQuadTo(5.29f, 6.71f)
                        reflectiveQuadTo(6f, 7f)
                        reflectiveQuadTo(6.71f, 6.71f)
                        close()
                        moveToRelative(0f, 6f)
                        quadTo(7f, 12.43f, 7f, 12f)
                        reflectiveQuadTo(6.71f, 11.29f)
                        reflectiveQuadTo(6f, 11f)
                        quadTo(5.58f, 11f, 5.29f, 11.29f)
                        reflectiveQuadTo(5f, 12f)
                        reflectiveQuadToRelative(0.29f, 0.71f)
                        reflectiveQuadTo(6f, 13f)
                        reflectiveQuadTo(6.71f, 12.71f)
                        close()
                        moveToRelative(0f, 6f)
                        quadTo(7f, 18.43f, 7f, 18f)
                        reflectiveQuadTo(6.71f, 17.29f)
                        reflectiveQuadTo(6f, 17f)
                        quadTo(5.58f, 17f, 5.29f, 17.29f)
                        reflectiveQuadTo(5f, 18f)
                        reflectiveQuadToRelative(0.29f, 0.71f)
                        reflectiveQuadTo(6f, 19f)
                        reflectiveQuadTo(6.71f, 18.71f)
                        close()
                    }
                }
                .build()
        return _storage!!
    }

private var _storage: ImageVector? = null
