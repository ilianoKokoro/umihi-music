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
val timer: ImageVector
    get() {
        if (_timer != null) {
            return _timer!!
        }
        _timer =
            ImageVector.Builder(
                name = "timer",
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
                        moveTo(10f, 3f)
                        quadTo(9.58f, 3f, 9.29f, 2.71f)
                        reflectiveQuadTo(9f, 2f)
                        quadTo(9f, 1.57f, 9.29f, 1.29f)
                        quadTo(9.58f, 1f, 10f, 1f)
                        horizontalLineToRelative(4f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(15f, 2f)
                        quadToRelative(0f, 0.42f, -0.29f, 0.71f)
                        reflectiveQuadTo(14f, 3f)
                        horizontalLineTo(10f)
                        close()
                        moveToRelative(2.71f, 10.71f)
                        quadTo(13f, 13.43f, 13f, 13f)
                        verticalLineTo(9f)
                        quadTo(13f, 8.57f, 12.71f, 8.29f)
                        reflectiveQuadTo(12f, 8f)
                        reflectiveQuadTo(11.29f, 8.29f)
                        reflectiveQuadTo(11f, 9f)
                        verticalLineToRelative(4f)
                        quadToRelative(0f, 0.42f, 0.29f, 0.71f)
                        reflectiveQuadTo(12f, 14f)
                        reflectiveQuadToRelative(0.71f, -0.29f)
                        close()
                        moveToRelative(-4.2f, 7.58f)
                        quadTo(6.88f, 20.58f, 5.65f, 19.35f)
                        reflectiveQuadTo(3.71f, 16.49f)
                        reflectiveQuadTo(3f, 13f)
                        reflectiveQuadTo(3.71f, 9.51f)
                        reflectiveQuadTo(5.65f, 6.65f)
                        quadTo(6.88f, 5.43f, 8.51f, 4.71f)
                        reflectiveQuadTo(12f, 4f)
                        quadToRelative(1.55f, 0f, 2.98f, 0.5f)
                        reflectiveQuadToRelative(2.68f, 1.45f)
                        lineToRelative(0.7f, -0.7f)
                        quadToRelative(0.27f, -0.28f, 0.7f, -0.28f)
                        reflectiveQuadToRelative(0.7f, 0.28f)
                        quadToRelative(0.28f, 0.27f, 0.28f, 0.7f)
                        reflectiveQuadToRelative(-0.28f, 0.7f)
                        lineToRelative(-0.7f, 0.7f)
                        quadTo(20f, 8.6f, 20.5f, 10.02f)
                        reflectiveQuadTo(21f, 13f)
                        quadToRelative(0f, 1.85f, -0.71f, 3.49f)
                        reflectiveQuadToRelative(-1.94f, 2.86f)
                        reflectiveQuadToRelative(-2.86f, 1.94f)
                        reflectiveQuadTo(12f, 22f)
                        reflectiveQuadTo(8.51f, 21.29f)
                        close()
                    }
                }
                .build()
        return _timer!!
    }

private var _timer: ImageVector? = null
