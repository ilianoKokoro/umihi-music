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
public val radio: ImageVector
  get() {
    if (_radio != null) {
      return _radio!!
    }
    _radio =
      ImageVector.Builder(
          name = "radio",
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
            moveTo(4f, 22f)
            quadTo(3.18f, 22f, 2.59f, 21.41f)
            reflectiveQuadTo(2f, 20f)
            verticalLineTo(6.65f)
            lineTo(15.08f, 1.32f)
            quadTo(15.43f, 1.2f, 15.76f, 1.34f)
            reflectiveQuadToRelative(0.46f, 0.49f)
            quadToRelative(0.13f, 0.35f, -0.01f, 0.69f)
            reflectiveQuadTo(15.73f, 2.97f)
            lineTo(8.3f, 6f)
            horizontalLineTo(20f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            quadTo(22f, 7.18f, 22f, 8f)
            verticalLineTo(20f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(20f, 22f)
            horizontalLineTo(4f)
            close()
            moveTo(9.78f, 18.27f)
            quadTo(10.5f, 17.55f, 10.5f, 16.5f)
            reflectiveQuadTo(9.78f, 14.73f)
            reflectiveQuadTo(8f, 14f)
            reflectiveQuadTo(6.23f, 14.73f)
            reflectiveQuadTo(5.5f, 16.5f)
            reflectiveQuadToRelative(0.73f, 1.77f)
            reflectiveQuadTo(8f, 19f)
            reflectiveQuadTo(9.78f, 18.27f)
            close()
            moveTo(4f, 11f)
            horizontalLineTo(16f)
            verticalLineTo(10f)
            quadTo(16f, 9.57f, 16.29f, 9.29f)
            reflectiveQuadTo(17f, 9f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(18f, 10f)
            verticalLineToRelative(1f)
            horizontalLineToRelative(2f)
            verticalLineTo(8f)
            horizontalLineTo(4f)
            verticalLineToRelative(3f)
            close()
          }
        }
        .build()
    return _radio!!
  }

private var _radio: ImageVector? = null
