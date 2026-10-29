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
val memory: ImageVector
    get() {
        if (_memory != null) {
            return _memory!!
        }
        _memory =
            ImageVector.Builder(
                name = "memory",
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
                        moveTo(9f, 14f)
                        verticalLineTo(10f)
                        quadTo(9f, 9.57f, 9.29f, 9.29f)
                        quadTo(9.58f, 9f, 10f, 9f)
                        horizontalLineToRelative(4f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(15f, 10f)
                        verticalLineToRelative(4f)
                        quadToRelative(0f, 0.42f, -0.29f, 0.71f)
                        reflectiveQuadTo(14f, 15f)
                        horizontalLineTo(10f)
                        quadTo(9.58f, 15f, 9.29f, 14.71f)
                        reflectiveQuadTo(9f, 14f)
                        close()
                        moveToRelative(0f, 6f)
                        verticalLineTo(19f)
                        horizontalLineTo(7f)
                        quadTo(6.18f, 19f, 5.59f, 18.41f)
                        reflectiveQuadTo(5f, 17f)
                        verticalLineTo(15f)
                        horizontalLineTo(4f)
                        quadTo(3.58f, 15f, 3.29f, 14.71f)
                        reflectiveQuadTo(3f, 14f)
                        reflectiveQuadTo(3.29f, 13.29f)
                        reflectiveQuadTo(4f, 13f)
                        horizontalLineTo(5f)
                        verticalLineTo(11f)
                        horizontalLineTo(4f)
                        quadTo(3.58f, 11f, 3.29f, 10.71f)
                        quadTo(3f, 10.43f, 3f, 10f)
                        quadTo(3f, 9.57f, 3.29f, 9.29f)
                        reflectiveQuadTo(4f, 9f)
                        horizontalLineTo(5f)
                        verticalLineTo(7f)
                        quadTo(5f, 6.18f, 5.59f, 5.59f)
                        reflectiveQuadTo(7f, 5f)
                        horizontalLineTo(9f)
                        verticalLineTo(4f)
                        quadTo(9f, 3.57f, 9.29f, 3.29f)
                        quadTo(9.58f, 3f, 10f, 3f)
                        reflectiveQuadToRelative(0.71f, 0.29f)
                        reflectiveQuadTo(11f, 4f)
                        verticalLineTo(5f)
                        horizontalLineToRelative(2f)
                        verticalLineTo(4f)
                        quadTo(13f, 3.57f, 13.29f, 3.29f)
                        reflectiveQuadTo(14f, 3f)
                        reflectiveQuadToRelative(0.71f, 0.29f)
                        reflectiveQuadTo(15f, 4f)
                        verticalLineTo(5f)
                        horizontalLineToRelative(2f)
                        quadToRelative(0.82f, 0f, 1.41f, 0.59f)
                        quadTo(19f, 6.18f, 19f, 7f)
                        verticalLineTo(9f)
                        horizontalLineToRelative(1f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(21f, 10f)
                        reflectiveQuadToRelative(-0.29f, 0.71f)
                        reflectiveQuadTo(20f, 11f)
                        horizontalLineTo(19f)
                        verticalLineToRelative(2f)
                        horizontalLineToRelative(1f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(21f, 14f)
                        reflectiveQuadToRelative(-0.29f, 0.71f)
                        reflectiveQuadTo(20f, 15f)
                        horizontalLineTo(19f)
                        verticalLineToRelative(2f)
                        quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                        reflectiveQuadTo(17f, 19f)
                        horizontalLineTo(15f)
                        verticalLineToRelative(1f)
                        quadToRelative(0f, 0.43f, -0.29f, 0.71f)
                        reflectiveQuadTo(14f, 21f)
                        reflectiveQuadTo(13.29f, 20.71f)
                        quadTo(13f, 20.43f, 13f, 20f)
                        verticalLineTo(19f)
                        horizontalLineTo(11f)
                        verticalLineToRelative(1f)
                        quadToRelative(0f, 0.43f, -0.29f, 0.71f)
                        reflectiveQuadTo(10f, 21f)
                        quadTo(9.58f, 21f, 9.29f, 20.71f)
                        quadTo(9f, 20.43f, 9f, 20f)
                        close()
                        moveToRelative(8f, -3f)
                        verticalLineTo(7f)
                        horizontalLineTo(7f)
                        verticalLineTo(17f)
                        horizontalLineTo(17f)
                        close()
                    }
                }
                .build()
        return _memory!!
    }

private var _memory: ImageVector? = null
