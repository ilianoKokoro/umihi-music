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
public val playlist_remove: ImageVector
  get() {
    if (_playlist_remove != null) {
      return _playlist_remove!!
    }
    _playlist_remove =
      ImageVector.Builder(
          name = "playlist_remove",
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
            moveTo(17f, 19.4f)
            lineToRelative(-1.9f, 1.9f)
            quadToRelative(-0.28f, 0.27f, -0.7f, 0.27f)
            reflectiveQuadTo(13.7f, 21.3f)
            reflectiveQuadTo(13.43f, 20.6f)
            reflectiveQuadTo(13.7f, 19.9f)
            lineTo(15.6f, 18f)
            lineTo(13.7f, 16.1f)
            quadTo(13.43f, 15.83f, 13.43f, 15.4f)
            reflectiveQuadTo(13.7f, 14.7f)
            reflectiveQuadToRelative(0.7f, -0.28f)
            reflectiveQuadToRelative(0.7f, 0.28f)
            lineTo(17f, 16.6f)
            lineToRelative(1.9f, -1.9f)
            quadToRelative(0.27f, -0.28f, 0.7f, -0.28f)
            reflectiveQuadToRelative(0.7f, 0.28f)
            reflectiveQuadToRelative(0.28f, 0.7f)
            reflectiveQuadTo(20.3f, 16.1f)
            lineTo(18.4f, 18f)
            lineToRelative(1.9f, 1.9f)
            quadToRelative(0.28f, 0.28f, 0.28f, 0.7f)
            reflectiveQuadTo(20.3f, 21.3f)
            quadToRelative(-0.27f, 0.27f, -0.7f, 0.27f)
            reflectiveQuadTo(18.9f, 21.3f)
            lineTo(17f, 19.4f)
            close()
            moveTo(4f, 16f)
            quadTo(3.58f, 16f, 3.29f, 15.71f)
            reflectiveQuadTo(3f, 15f)
            reflectiveQuadTo(3.29f, 14.29f)
            reflectiveQuadTo(4f, 14f)
            horizontalLineTo(9f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(10f, 15f)
            reflectiveQuadTo(9.71f, 15.71f)
            reflectiveQuadTo(9f, 16f)
            horizontalLineTo(4f)
            close()
            moveTo(4f, 12f)
            quadTo(3.58f, 12f, 3.29f, 11.71f)
            quadTo(3f, 11.43f, 3f, 11f)
            reflectiveQuadTo(3.29f, 10.29f)
            reflectiveQuadTo(4f, 10f)
            horizontalLineToRelative(9f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(14f, 11f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(13f, 12f)
            horizontalLineTo(4f)
            close()
            moveTo(4f, 8f)
            quadTo(3.58f, 8f, 3.29f, 7.71f)
            quadTo(3f, 7.43f, 3f, 7f)
            reflectiveQuadTo(3.29f, 6.29f)
            reflectiveQuadTo(4f, 6f)
            horizontalLineToRelative(9f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(14f, 7f)
            reflectiveQuadTo(13.71f, 7.71f)
            reflectiveQuadTo(13f, 8f)
            horizontalLineTo(4f)
            close()
          }
        }
        .build()
    return _playlist_remove!!
  }

private var _playlist_remove: ImageVector? = null
