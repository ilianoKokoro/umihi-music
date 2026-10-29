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
public val keyboard_arrow_down: ImageVector
  get() {
    if (_keyboard_arrow_down != null) {
      return _keyboard_arrow_down!!
    }
    _keyboard_arrow_down =
      ImageVector.Builder(
          name = "keyboard_arrow_down",
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
            moveTo(11.63f, 14.91f)
            quadTo(11.45f, 14.85f, 11.3f, 14.7f)
            lineTo(6.7f, 10.1f)
            quadTo(6.43f, 9.82f, 6.43f, 9.4f)
            quadTo(6.43f, 8.98f, 6.7f, 8.7f)
            reflectiveQuadTo(7.4f, 8.42f)
            reflectiveQuadTo(8.1f, 8.7f)
            lineTo(12f, 12.6f)
            lineTo(15.9f, 8.7f)
            quadTo(16.18f, 8.42f, 16.6f, 8.42f)
            reflectiveQuadTo(17.3f, 8.7f)
            reflectiveQuadToRelative(0.27f, 0.7f)
            reflectiveQuadTo(17.3f, 10.1f)
            lineToRelative(-4.6f, 4.6f)
            quadToRelative(-0.15f, 0.15f, -0.33f, 0.21f)
            reflectiveQuadTo(12f, 14.98f)
            reflectiveQuadTo(11.63f, 14.91f)
            close()
          }
        }
        .build()
    return _keyboard_arrow_down!!
  }

private var _keyboard_arrow_down: ImageVector? = null
