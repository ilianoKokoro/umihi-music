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
public val visibility_off: ImageVector
  get() {
    if (_visibility_off != null) {
      return _visibility_off!!
    }
    _visibility_off =
      ImageVector.Builder(
          name = "visibility_off",
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
            moveTo(19.1f, 21.9f)
            lineTo(15.6f, 18.45f)
            quadToRelative(-0.88f, 0.28f, -1.78f, 0.41f)
            reflectiveQuadTo(12f, 19f)
            quadTo(8.65f, 19f, 5.88f, 17.2f)
            reflectiveQuadTo(1.53f, 12.45f)
            quadTo(1.4f, 12.23f, 1.34f, 11.99f)
            reflectiveQuadTo(1.28f, 11.5f)
            reflectiveQuadTo(1.34f, 11.01f)
            reflectiveQuadTo(1.53f, 10.55f)
            quadTo(2.08f, 9.57f, 2.7f, 8.65f)
            reflectiveQuadTo(4.15f, 7f)
            lineTo(2.08f, 4.9f)
            quadTo(1.8f, 4.63f, 1.8f, 4.21f)
            reflectiveQuadTo(2.1f, 3.5f)
            quadTo(2.38f, 3.22f, 2.8f, 3.22f)
            reflectiveQuadTo(3.5f, 3.5f)
            lineToRelative(17f, 17f)
            quadToRelative(0.28f, 0.27f, 0.29f, 0.69f)
            reflectiveQuadTo(20.5f, 21.9f)
            quadToRelative(-0.27f, 0.28f, -0.7f, 0.28f)
            reflectiveQuadTo(19.1f, 21.9f)
            close()
            moveTo(12f, 16f)
            quadToRelative(0.28f, 0f, 0.53f, -0.03f)
            reflectiveQuadToRelative(0.5f, -0.1f)
            lineToRelative(-5.4f, -5.4f)
            quadToRelative(-0.07f, 0.25f, -0.1f, 0.5f)
            reflectiveQuadTo(7.5f, 11.5f)
            quadToRelative(0f, 1.88f, 1.31f, 3.19f)
            reflectiveQuadTo(12f, 16f)
            close()
            moveTo(12f, 4f)
            quadToRelative(3.35f, 0f, 6.14f, 1.81f)
            quadToRelative(2.79f, 1.81f, 4.36f, 4.76f)
            quadToRelative(0.13f, 0.2f, 0.19f, 0.44f)
            reflectiveQuadToRelative(0.06f, 0.49f)
            reflectiveQuadTo(22.7f, 11.99f)
            quadToRelative(-0.05f, 0.24f, -0.18f, 0.44f)
            quadToRelative(-0.48f, 0.93f, -1.06f, 1.75f)
            reflectiveQuadToRelative(-1.31f, 1.55f)
            quadToRelative(-0.35f, 0.35f, -0.82f, 0.33f)
            quadTo(18.85f, 16.02f, 18.5f, 15.68f)
            lineToRelative(-2f, -2f)
            quadTo(16.33f, 13.5f, 16.28f, 13.26f)
            reflectiveQuadTo(16.3f, 12.77f)
            quadToRelative(0.1f, -0.32f, 0.15f, -0.63f)
            reflectiveQuadTo(16.5f, 11.5f)
            quadToRelative(0f, -1.88f, -1.31f, -3.19f)
            reflectiveQuadTo(12f, 7f)
            quadTo(11.65f, 7f, 11.35f, 7.05f)
            reflectiveQuadTo(10.73f, 7.2f)
            quadToRelative(-0.25f, 0.07f, -0.5f, 0.02f)
            quadTo(9.98f, 7.18f, 9.8f, 7f)
            lineTo(8.98f, 6.18f)
            quadTo(8.5f, 5.7f, 8.66f, 5.07f)
            quadTo(8.83f, 4.45f, 9.45f, 4.27f)
            quadToRelative(0.63f, -0.13f, 1.26f, -0.2f)
            reflectiveQuadTo(12f, 4f)
            close()
            moveToRelative(1.98f, 5.65f)
            quadToRelative(0.27f, 0.33f, 0.46f, 0.71f)
            reflectiveQuadToRelative(0.24f, 0.81f)
            quadToRelative(0.03f, 0.2f, -0.15f, 0.28f)
            reflectiveQuadTo(14.2f, 11.38f)
            lineTo(12.15f, 9.32f)
            quadTo(12f, 9.17f, 12.09f, 9f)
            quadTo(12.18f, 8.82f, 12.38f, 8.82f)
            quadToRelative(0.48f, 0.05f, 0.88f, 0.26f)
            reflectiveQuadToRelative(0.73f, 0.56f)
            close()
          }
        }
        .build()
    return _visibility_off!!
  }

private var _visibility_off: ImageVector? = null
