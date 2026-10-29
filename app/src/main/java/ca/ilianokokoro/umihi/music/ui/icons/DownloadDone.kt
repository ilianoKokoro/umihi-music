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
public val download_done: ImageVector
  get() {
    if (_download_done != null) {
      return _download_done!!
    }
    _download_done =
      ImageVector.Builder(
          name = "download_done",
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
            moveTo(9.55f, 13.15f)
            lineTo(18f, 4.7f)
            quadTo(18.3f, 4.4f, 18.71f, 4.4f)
            reflectiveQuadToRelative(0.71f, 0.3f)
            reflectiveQuadToRelative(0.3f, 0.71f)
            reflectiveQuadToRelative(-0.3f, 0.71f)
            lineTo(10.25f, 15.3f)
            quadToRelative(-0.3f, 0.3f, -0.7f, 0.3f)
            reflectiveQuadTo(8.85f, 15.3f)
            lineTo(4.58f, 11.02f)
            quadTo(4.28f, 10.73f, 4.29f, 10.31f)
            reflectiveQuadTo(4.6f, 9.6f)
            reflectiveQuadTo(5.31f, 9.3f)
            reflectiveQuadTo(6.03f, 9.6f)
            lineToRelative(3.53f, 3.55f)
            close()
            moveTo(6f, 20f)
            quadTo(5.58f, 20f, 5.29f, 19.71f)
            quadTo(5f, 19.43f, 5f, 19f)
            reflectiveQuadTo(5.29f, 18.29f)
            reflectiveQuadTo(6f, 18f)
            horizontalLineTo(18f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(19f, 19f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(18f, 20f)
            horizontalLineTo(6f)
            close()
          }
        }
        .build()
    return _download_done!!
  }

private var _download_done: ImageVector? = null
