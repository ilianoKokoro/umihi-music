package ca.ilianokokoro.umihi.music.extensions

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout

fun Modifier.placedWhen(active: Boolean): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        if (active) {
            layout(placeable.width, placeable.height) { placeable.place(0, 0) }
        } else {
            layout(0, 0) {}
        }
    }