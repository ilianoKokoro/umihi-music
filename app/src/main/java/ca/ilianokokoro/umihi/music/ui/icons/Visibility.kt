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
public val visibility: ImageVector
  get() {
    if (_visibility != null) {
      return _visibility!!
    }
    _visibility =
      ImageVector.Builder(
          name = "visibility",
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
            moveTo(15.19f, 14.69f)
            quadTo(16.5f, 13.38f, 16.5f, 11.5f)
            reflectiveQuadTo(15.19f, 8.31f)
            reflectiveQuadTo(12f, 7f)
            reflectiveQuadTo(8.81f, 8.31f)
            reflectiveQuadTo(7.5f, 11.5f)
            reflectiveQuadToRelative(1.31f, 3.19f)
            reflectiveQuadTo(12f, 16f)
            reflectiveQuadToRelative(3.19f, -1.31f)
            close()
            moveToRelative(-5.1f, -1.28f)
            quadTo(9.3f, 12.63f, 9.3f, 11.5f)
            reflectiveQuadTo(10.09f, 9.59f)
            reflectiveQuadTo(12f, 8.8f)
            reflectiveQuadToRelative(1.91f, 0.79f)
            quadToRelative(0.79f, 0.79f, 0.79f, 1.91f)
            reflectiveQuadToRelative(-0.79f, 1.91f)
            reflectiveQuadTo(12f, 14.2f)
            reflectiveQuadTo(10.09f, 13.41f)
            close()
            moveTo(5.89f, 17.2f)
            quadTo(3.13f, 15.4f, 1.53f, 12.45f)
            quadTo(1.4f, 12.23f, 1.34f, 11.99f)
            reflectiveQuadTo(1.28f, 11.5f)
            reflectiveQuadTo(1.34f, 11.01f)
            reflectiveQuadTo(1.53f, 10.55f)
            quadTo(3.13f, 7.6f, 5.89f, 5.8f)
            reflectiveQuadTo(12f, 4f)
            reflectiveQuadToRelative(6.11f, 1.8f)
            reflectiveQuadToRelative(4.36f, 4.75f)
            quadToRelative(0.13f, 0.22f, 0.19f, 0.46f)
            reflectiveQuadToRelative(0.06f, 0.49f)
            reflectiveQuadToRelative(-0.06f, 0.49f)
            quadToRelative(-0.06f, 0.24f, -0.19f, 0.46f)
            quadToRelative(-1.6f, 2.95f, -4.36f, 4.75f)
            reflectiveQuadTo(12f, 19f)
            reflectiveQuadTo(5.89f, 17.2f)
            close()
          }
        }
        .build()
    return _visibility!!
  }

private var _visibility: ImageVector? = null
