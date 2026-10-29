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
public val brightness_auto: ImageVector
  get() {
    if (_brightness_auto != null) {
      return _brightness_auto!!
    }
    _brightness_auto =
      ImageVector.Builder(
          name = "brightness_auto",
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
            moveTo(8.65f, 20f)
            horizontalLineTo(6f)
            quadTo(5.18f, 20f, 4.59f, 19.41f)
            reflectiveQuadTo(4f, 18f)
            verticalLineTo(15.35f)
            lineTo(2.08f, 13.4f)
            quadTo(1.8f, 13.1f, 1.65f, 12.74f)
            quadTo(1.5f, 12.38f, 1.5f, 12f)
            reflectiveQuadTo(1.65f, 11.26f)
            reflectiveQuadTo(2.08f, 10.6f)
            lineTo(4f, 8.65f)
            verticalLineTo(6f)
            quadTo(4f, 5.18f, 4.59f, 4.59f)
            reflectiveQuadTo(6f, 4f)
            horizontalLineTo(8.65f)
            lineTo(10.6f, 2.07f)
            quadTo(10.9f, 1.8f, 11.26f, 1.65f)
            reflectiveQuadTo(12f, 1.5f)
            reflectiveQuadToRelative(0.74f, 0.15f)
            reflectiveQuadTo(13.4f, 2.07f)
            lineTo(15.35f, 4f)
            horizontalLineTo(18f)
            quadToRelative(0.82f, 0f, 1.41f, 0.59f)
            quadTo(20f, 5.18f, 20f, 6f)
            verticalLineTo(8.65f)
            lineToRelative(1.93f, 1.95f)
            quadToRelative(0.28f, 0.3f, 0.43f, 0.66f)
            reflectiveQuadTo(22.5f, 12f)
            reflectiveQuadToRelative(-0.15f, 0.74f)
            reflectiveQuadTo(21.93f, 13.4f)
            lineTo(20f, 15.35f)
            verticalLineTo(18f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(18f, 20f)
            horizontalLineTo(15.35f)
            lineTo(13.4f, 21.93f)
            quadToRelative(-0.3f, 0.27f, -0.66f, 0.43f)
            reflectiveQuadTo(12f, 22.5f)
            reflectiveQuadTo(11.26f, 22.35f)
            reflectiveQuadTo(10.6f, 21.93f)
            lineTo(8.65f, 20f)
            close()
            moveTo(10.2f, 13.7f)
            horizontalLineToRelative(3.65f)
            lineToRelative(0.63f, 1.83f)
            quadToRelative(0.07f, 0.2f, 0.26f, 0.34f)
            reflectiveQuadTo(15.15f, 16f)
            quadToRelative(0.38f, 0f, 0.59f, -0.31f)
            reflectiveQuadToRelative(0.09f, -0.66f)
            lineTo(12.98f, 7.47f)
            quadTo(12.9f, 7.25f, 12.7f, 7.13f)
            reflectiveQuadTo(12.28f, 7f)
            horizontalLineTo(11.73f)
            quadTo(11.5f, 7f, 11.3f, 7.13f)
            reflectiveQuadTo(11.03f, 7.47f)
            lineTo(8.18f, 15f)
            quadToRelative(-0.13f, 0.35f, 0.09f, 0.67f)
            reflectiveQuadTo(8.88f, 16f)
            quadToRelative(0.25f, 0f, 0.44f, -0.14f)
            quadTo(9.5f, 15.73f, 9.58f, 15.5f)
            lineTo(10.2f, 13.7f)
            close()
            moveToRelative(0.45f, -1.3f)
            lineToRelative(1.3f, -3.75f)
            horizontalLineToRelative(0.1f)
            lineToRelative(1.3f, 3.75f)
            horizontalLineToRelative(-2.7f)
            close()
          }
        }
        .build()
    return _brightness_auto!!
  }

private var _brightness_auto: ImageVector? = null
