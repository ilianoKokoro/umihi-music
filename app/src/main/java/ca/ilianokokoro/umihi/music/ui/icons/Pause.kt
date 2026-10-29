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
public val pause: ImageVector
  get() {
    if (_pause != null) {
      return _pause!!
    }
    _pause =
      ImageVector.Builder(
          name = "pause",
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
            moveTo(16f, 19f)
            quadToRelative(-0.82f, 0f, -1.41f, -0.59f)
            reflectiveQuadTo(14f, 17f)
            verticalLineTo(7f)
            quadTo(14f, 6.18f, 14.59f, 5.59f)
            reflectiveQuadTo(16f, 5f)
            quadToRelative(0.82f, 0f, 1.41f, 0.59f)
            quadTo(18f, 6.18f, 18f, 7f)
            verticalLineTo(17f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(16f, 19f)
            close()
            moveTo(8f, 19f)
            quadTo(7.18f, 19f, 6.59f, 18.41f)
            reflectiveQuadTo(6f, 17f)
            verticalLineTo(7f)
            quadTo(6f, 6.18f, 6.59f, 5.59f)
            reflectiveQuadTo(8f, 5f)
            quadTo(8.83f, 5f, 9.41f, 5.59f)
            quadTo(10f, 6.18f, 10f, 7f)
            verticalLineTo(17f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            quadTo(8.83f, 19f, 8f, 19f)
            close()
          }
        }
        .build()
    return _pause!!
  }

private var _pause: ImageVector? = null