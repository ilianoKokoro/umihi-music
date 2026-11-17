package ca.ilianokokoro.umihi.music.extensions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import ca.ilianokokoro.umihi.music.core.Constants
import okio.Buffer

fun ByteArray.cappedTo(maxSize: Int = Constants.Ui.WEAROS_MAX_IMAGE_SIZE): ByteArray? {
    val bitmap = BitmapFactory.decodeByteArray(this, 0, size) ?: return null

    val capped = if (bitmap.width <= maxSize && bitmap.height <= maxSize) {
        bitmap
    } else {
        val scale = maxSize.toFloat() / maxOf(bitmap.width, bitmap.height)
        bitmap.scale((bitmap.width * scale).toInt(), (bitmap.height * scale).toInt())
    }

    val buffer = Buffer()
    capped.compress(Bitmap.CompressFormat.JPEG, 90, buffer.outputStream())

    return buffer.readByteArray()
}