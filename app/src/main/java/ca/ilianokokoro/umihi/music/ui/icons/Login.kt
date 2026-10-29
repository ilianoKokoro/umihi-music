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
public val login: ImageVector
  get() {
    if (_login != null) {
      return _login!!
    }
    _login =
      ImageVector.Builder(
          name = "login",
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
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(13f, 21f)
            quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
            quadTo(12f, 20.43f, 12f, 20f)
            reflectiveQuadToRelative(0.29f, -0.71f)
            reflectiveQuadTo(13f, 19f)
            horizontalLineToRelative(6f)
            verticalLineTo(5f)
            horizontalLineTo(13f)
            quadTo(12.58f, 5f, 12.29f, 4.71f)
            reflectiveQuadTo(12f, 4f)
            quadTo(12f, 3.57f, 12.29f, 3.29f)
            reflectiveQuadTo(13f, 3f)
            horizontalLineToRelative(6f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(21f, 5f)
            verticalLineTo(19f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(19f, 21f)
            horizontalLineTo(13f)
            close()
            moveTo(11.18f, 13f)
            horizontalLineTo(4f)
            quadTo(3.58f, 13f, 3.29f, 12.71f)
            quadTo(3f, 12.43f, 3f, 12f)
            reflectiveQuadTo(3.29f, 11.29f)
            reflectiveQuadTo(4f, 11f)
            horizontalLineToRelative(7.18f)
            lineTo(9.3f, 9.13f)
            quadTo(9.03f, 8.85f, 9.03f, 8.45f)
            reflectiveQuadTo(9.3f, 7.75f)
            reflectiveQuadTo(10f, 7.44f)
            reflectiveQuadToRelative(0.73f, 0.29f)
            lineTo(14.3f, 11.3f)
            quadToRelative(0.3f, 0.3f, 0.3f, 0.7f)
            reflectiveQuadToRelative(-0.3f, 0.7f)
            lineToRelative(-3.57f, 3.57f)
            quadToRelative(-0.3f, 0.3f, -0.71f, 0.29f)
            reflectiveQuadTo(9.3f, 16.25f)
            quadTo(9.03f, 15.95f, 9.04f, 15.54f)
            reflectiveQuadTo(9.33f, 14.85f)
            lineTo(11.18f, 13f)
            close()
          }
        }
        .build()
    return _login!!
  }

private var _login: ImageVector? = null
