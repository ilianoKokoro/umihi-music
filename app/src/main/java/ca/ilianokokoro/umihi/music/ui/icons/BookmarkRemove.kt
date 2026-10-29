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
val bookmark_remove: ImageVector
    get() {
        if (_bookmark_remove != null) {
            return _bookmark_remove!!
        }
        _bookmark_remove =
            ImageVector.Builder(
                name = "bookmark_remove",
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
                        moveTo(16f, 7f)
                        quadTo(15.58f, 7f, 15.29f, 6.71f)
                        quadTo(15f, 6.43f, 15f, 6f)
                        reflectiveQuadTo(15.29f, 5.29f)
                        reflectiveQuadTo(16f, 5f)
                        horizontalLineToRelative(4f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(21f, 6f)
                        reflectiveQuadTo(20.71f, 6.71f)
                        reflectiveQuadTo(20f, 7f)
                        horizontalLineTo(16f)
                        close()
                        moveTo(12f, 18f)
                        lineTo(7.8f, 19.8f)
                        quadToRelative(-1f, 0.43f, -1.9f, -0.16f)
                        reflectiveQuadTo(5f, 17.98f)
                        verticalLineTo(5f)
                        quadTo(5f, 4.17f, 5.59f, 3.59f)
                        reflectiveQuadTo(7f, 3f)
                        horizontalLineToRelative(5.63f)
                        quadToRelative(0.45f, 0f, 0.68f, 0.4f)
                        reflectiveQuadToRelative(0.03f, 0.82f)
                        quadTo(13.15f, 4.65f, 13.08f, 5.07f)
                        reflectiveQuadTo(13f, 6f)
                        quadToRelative(0f, 1.8f, 1.14f, 3.17f)
                        reflectiveQuadTo(17f, 10.9f)
                        quadToRelative(0.3f, 0.05f, 0.54f, 0.06f)
                        quadToRelative(0.24f, 0.01f, 0.46f, 0.01f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.26f)
                        reflectiveQuadTo(19f, 11.9f)
                        verticalLineToRelative(6.08f)
                        quadToRelative(0f, 1.07f, -0.9f, 1.66f)
                        quadToRelative(-0.9f, 0.59f, -1.9f, 0.16f)
                        lineTo(12f, 18f)
                        close()
                    }
                }
                .build()
        return _bookmark_remove!!
    }

private var _bookmark_remove: ImageVector? = null
