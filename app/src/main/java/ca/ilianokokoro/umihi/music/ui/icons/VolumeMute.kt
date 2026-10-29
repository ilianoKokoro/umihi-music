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
val volume_mute: ImageVector
    get() {
        if (_volume_mute != null) {
            return _volume_mute!!
        }
        _volume_mute =
            ImageVector.Builder(
                name = "volume_mute",
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
                        moveTo(11f, 15f)
                        horizontalLineTo(8f)
                        quadTo(7.58f, 15f, 7.29f, 14.71f)
                        reflectiveQuadTo(7f, 14f)
                        verticalLineTo(10f)
                        quadTo(7f, 9.57f, 7.29f, 9.29f)
                        reflectiveQuadTo(8f, 9f)
                        horizontalLineToRelative(3f)
                        lineTo(14.3f, 5.7f)
                        quadTo(14.78f, 5.22f, 15.39f, 5.49f)
                        reflectiveQuadTo(16f, 6.43f)
                        verticalLineTo(17.58f)
                        quadToRelative(0f, 0.68f, -0.61f, 0.94f)
                        reflectiveQuadTo(14.3f, 18.3f)
                        lineTo(11f, 15f)
                        close()
                    }
                }
                .build()
        return _volume_mute!!
    }

private var _volume_mute: ImageVector? = null
