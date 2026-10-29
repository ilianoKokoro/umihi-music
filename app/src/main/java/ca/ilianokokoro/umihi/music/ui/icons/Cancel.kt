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
public val cancel: ImageVector
  get() {
    if (_cancel != null) {
      return _cancel!!
    }
    _cancel =
      ImageVector.Builder(
          name = "cancel",
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
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(12f, 13.4f)
            lineToRelative(2.9f, 2.9f)
            quadToRelative(0.28f, 0.27f, 0.7f, 0.27f)
            reflectiveQuadTo(16.3f, 16.3f)
            quadToRelative(0.27f, -0.28f, 0.27f, -0.7f)
            reflectiveQuadTo(16.3f, 14.9f)
            lineTo(13.4f, 12f)
            lineTo(16.3f, 9.1f)
            quadTo(16.58f, 8.82f, 16.58f, 8.4f)
            reflectiveQuadTo(16.3f, 7.7f)
            reflectiveQuadTo(15.6f, 7.43f)
            reflectiveQuadTo(14.9f, 7.7f)
            lineTo(12f, 10.6f)
            lineTo(9.1f, 7.7f)
            quadTo(8.83f, 7.43f, 8.4f, 7.43f)
            reflectiveQuadTo(7.7f, 7.7f)
            reflectiveQuadTo(7.43f, 8.4f)
            reflectiveQuadTo(7.7f, 9.1f)
            lineTo(10.6f, 12f)
            lineTo(7.7f, 14.9f)
            quadTo(7.43f, 15.18f, 7.43f, 15.6f)
            reflectiveQuadTo(7.7f, 16.3f)
            reflectiveQuadToRelative(0.7f, 0.27f)
            quadToRelative(0.43f, 0f, 0.7f, -0.27f)
            lineTo(12f, 13.4f)
            close()
            moveTo(12f, 22f)
            quadTo(9.93f, 22f, 8.1f, 21.21f)
            quadTo(6.28f, 20.43f, 4.93f, 19.08f)
            quadTo(3.58f, 17.73f, 2.79f, 15.9f)
            reflectiveQuadTo(2f, 12f)
            quadTo(2f, 9.92f, 2.79f, 8.1f)
            quadTo(3.58f, 6.27f, 4.93f, 4.93f)
            quadTo(6.28f, 3.57f, 8.1f, 2.79f)
            quadTo(9.93f, 2f, 12f, 2f)
            reflectiveQuadToRelative(3.9f, 0.79f)
            reflectiveQuadToRelative(3.17f, 2.14f)
            quadToRelative(1.35f, 1.35f, 2.14f, 3.17f)
            quadTo(22f, 9.92f, 22f, 12f)
            reflectiveQuadToRelative(-0.79f, 3.9f)
            reflectiveQuadToRelative(-2.14f, 3.17f)
            quadToRelative(-1.35f, 1.35f, -3.17f, 2.14f)
            reflectiveQuadTo(12f, 22f)
            close()
          }
        }
        .build()
    return _cancel!!
  }

private var _cancel: ImageVector? = null
