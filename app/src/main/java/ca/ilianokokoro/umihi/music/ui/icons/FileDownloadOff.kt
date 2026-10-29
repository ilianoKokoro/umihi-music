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
public val file_download_off: ImageVector
  get() {
    if (_file_download_off != null) {
      return _file_download_off!!
    }
    _file_download_off =
      ImageVector.Builder(
          name = "file_download_off",
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
            moveTo(19.08f, 21.9f)
            lineTo(17.15f, 20f)
            horizontalLineTo(6f)
            quadTo(5.18f, 20f, 4.59f, 19.41f)
            reflectiveQuadTo(4f, 18f)
            verticalLineTo(16f)
            quadTo(4f, 15.58f, 4.29f, 15.29f)
            reflectiveQuadTo(5f, 15f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(6f, 16f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(9.15f)
            lineTo(12.58f, 15.43f)
            quadToRelative(-0.15f, 0.08f, -0.29f, 0.11f)
            reflectiveQuadTo(12f, 15.58f)
            quadToRelative(-0.25f, 0f, -0.41f, -0.07f)
            reflectiveQuadTo(11.3f, 15.3f)
            lineTo(7.7f, 11.7f)
            quadTo(7.43f, 11.43f, 7.41f, 11.06f)
            reflectiveQuadTo(7.6f, 10.45f)
            lineTo(2.08f, 4.93f)
            quadTo(1.8f, 4.65f, 1.8f, 4.22f)
            reflectiveQuadTo(2.1f, 3.5f)
            quadTo(2.38f, 3.22f, 2.8f, 3.22f)
            reflectiveQuadTo(3.53f, 3.5f)
            lineToRelative(16.98f, 17f)
            quadToRelative(0.28f, 0.27f, 0.28f, 0.7f)
            reflectiveQuadTo(20.5f, 21.9f)
            quadToRelative(-0.3f, 0.3f, -0.72f, 0.3f)
            reflectiveQuadToRelative(-0.7f, -0.3f)
            close()
            moveTo(16.3f, 10.3f)
            quadToRelative(0.27f, 0.28f, 0.27f, 0.7f)
            reflectiveQuadTo(16.3f, 11.7f)
            lineToRelative(-0.18f, 0.18f)
            quadToRelative(-0.3f, 0.3f, -0.7f, 0.29f)
            reflectiveQuadToRelative(-0.7f, -0.31f)
            reflectiveQuadToRelative(-0.3f, -0.71f)
            reflectiveQuadToRelative(0.3f, -0.71f)
            lineToRelative(0.15f, -0.15f)
            quadTo(15.15f, 10f, 15.59f, 10.01f)
            quadToRelative(0.44f, 0.01f, 0.71f, 0.29f)
            close()
            moveTo(12.71f, 4.29f)
            quadTo(13f, 4.57f, 13f, 5f)
            verticalLineTo(7.75f)
            quadToRelative(0f, 0.5f, -0.31f, 0.74f)
            quadTo(12.38f, 8.73f, 12f, 8.73f)
            reflectiveQuadTo(11.31f, 8.48f)
            reflectiveQuadTo(11f, 7.72f)
            verticalLineTo(5f)
            quadTo(11f, 4.57f, 11.29f, 4.29f)
            reflectiveQuadTo(12f, 4f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            close()
          }
        }
        .build()
    return _file_download_off!!
  }

private var _file_download_off: ImageVector? = null
