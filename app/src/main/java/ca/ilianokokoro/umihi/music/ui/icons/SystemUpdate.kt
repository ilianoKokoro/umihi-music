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
public val system_update: ImageVector
  get() {
    if (_system_update != null) {
      return _system_update!!
    }
    _system_update =
      ImageVector.Builder(
          name = "system_update",
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
            moveTo(7f, 23f)
            quadTo(6.18f, 23f, 5.59f, 22.41f)
            reflectiveQuadTo(5f, 21f)
            verticalLineTo(3f)
            quadTo(5f, 2.17f, 5.59f, 1.59f)
            reflectiveQuadTo(7f, 1f)
            horizontalLineTo(17f)
            quadToRelative(0.82f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(19f, 3f)
            verticalLineTo(6.1f)
            quadToRelative(0.45f, 0.18f, 0.73f, 0.55f)
            reflectiveQuadTo(20f, 7.5f)
            verticalLineToRelative(2f)
            quadToRelative(0f, 0.47f, -0.27f, 0.85f)
            reflectiveQuadTo(19f, 10.9f)
            verticalLineTo(21f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(17f, 23f)
            horizontalLineTo(7f)
            close()
            moveToRelative(5.38f, -7.49f)
            quadTo(12.55f, 15.45f, 12.7f, 15.3f)
            lineToRelative(2.6f, -2.6f)
            quadToRelative(0.28f, -0.28f, 0.29f, -0.69f)
            reflectiveQuadTo(15.3f, 11.3f)
            quadTo(15.03f, 11.02f, 14.61f, 11.01f)
            reflectiveQuadTo(13.9f, 11.27f)
            lineTo(13f, 12.15f)
            verticalLineTo(9f)
            quadTo(13f, 8.57f, 12.71f, 8.29f)
            reflectiveQuadTo(12f, 8f)
            reflectiveQuadTo(11.29f, 8.29f)
            reflectiveQuadTo(11f, 9f)
            verticalLineToRelative(3.15f)
            lineTo(10.1f, 11.27f)
            quadTo(9.83f, 11f, 9.41f, 11f)
            reflectiveQuadTo(8.7f, 11.3f)
            quadTo(8.43f, 11.58f, 8.43f, 12f)
            reflectiveQuadTo(8.7f, 12.7f)
            lineToRelative(2.6f, 2.6f)
            quadToRelative(0.15f, 0.15f, 0.32f, 0.21f)
            reflectiveQuadTo(12f, 15.58f)
            reflectiveQuadToRelative(0.38f, -0.06f)
            close()
          }
        }
        .build()
    return _system_update!!
  }

private var _system_update: ImageVector? = null
