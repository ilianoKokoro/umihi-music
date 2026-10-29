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
public val link: ImageVector
  get() {
    if (_link != null) {
      return _link!!
    }
    _link =
      ImageVector.Builder(
          name = "link",
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
            moveTo(7f, 17f)
            quadTo(4.93f, 17f, 3.46f, 15.54f)
            reflectiveQuadTo(2f, 12f)
            quadTo(2f, 9.92f, 3.46f, 8.46f)
            reflectiveQuadTo(7f, 7f)
            horizontalLineToRelative(3f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(11f, 8f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(10f, 9f)
            horizontalLineTo(7f)
            quadTo(5.75f, 9f, 4.88f, 9.88f)
            reflectiveQuadTo(4f, 12f)
            reflectiveQuadToRelative(0.88f, 2.13f)
            reflectiveQuadTo(7f, 15f)
            horizontalLineToRelative(3f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(11f, 16f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(10f, 17f)
            horizontalLineTo(7f)
            close()
            moveTo(9f, 13f)
            quadTo(8.58f, 13f, 8.29f, 12.71f)
            quadTo(8f, 12.43f, 8f, 12f)
            reflectiveQuadTo(8.29f, 11.29f)
            quadTo(8.58f, 11f, 9f, 11f)
            horizontalLineToRelative(6f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(16f, 12f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(15f, 13f)
            horizontalLineTo(9f)
            close()
            moveToRelative(5f, 4f)
            quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
            quadTo(13f, 16.43f, 13f, 16f)
            reflectiveQuadToRelative(0.29f, -0.71f)
            reflectiveQuadTo(14f, 15f)
            horizontalLineToRelative(3f)
            quadToRelative(1.25f, 0f, 2.13f, -0.88f)
            reflectiveQuadTo(20f, 12f)
            reflectiveQuadTo(19.13f, 9.88f)
            reflectiveQuadTo(17f, 9f)
            horizontalLineTo(14f)
            quadTo(13.58f, 9f, 13.29f, 8.71f)
            reflectiveQuadTo(13f, 8f)
            quadTo(13f, 7.57f, 13.29f, 7.29f)
            reflectiveQuadTo(14f, 7f)
            horizontalLineToRelative(3f)
            quadToRelative(2.07f, 0f, 3.54f, 1.46f)
            reflectiveQuadTo(22f, 12f)
            reflectiveQuadToRelative(-1.46f, 3.54f)
            reflectiveQuadTo(17f, 17f)
            horizontalLineTo(14f)
            close()
          }
        }
        .build()
    return _link!!
  }

private var _link: ImageVector? = null
